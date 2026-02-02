package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.application.port.GameTaskScheduler;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.exception.GameSystemException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.piece.application.PieceService;
import dev.tecte.chessWar.piece.application.port.PieceInfoRenderer;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import dev.tecte.chessWar.team.application.TeamService;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * 게임의 진행 및 생명주기를 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameService {
    private final GameNotifier gameNotifier;
    private final BoardService boardService;
    private final TeamService teamService;
    private final PieceService pieceService;
    private final GameRepository gameRepository;
    private final GameTaskScheduler gameTaskScheduler;
    private final PieceInfoRenderer pieceInfoRenderer;
    private final ExceptionDispatcher exceptionDispatcher;

    /**
     * 게임 시작 조건을 확인한 후 게임을 시작합니다.
     *
     * @param sender 시작을 요청한 주체
     * @throws GameException 게임 시작 조건을 충족하지 못했을 경우
     */
    @HandleException
    public void startGame(@NonNull CommandSender sender) {
        if (gameRepository.isGameInProgress()) {
            throw GameException.alreadyInProgress();
        }

        Board board = boardService.findBoard().orElseThrow(GameException::boardNotSetup);
        String worldName = board.worldName();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw GameException.worldNotFound(worldName);
        }

        int minPlayers = 1;

        if (!teamService.areAllTeamsReadyToStart(minPlayers)) {
            throw GameException.insufficientPlayers(minPlayers);
        }

        Game game = Game.create(board);

        gameRepository.save(game);
        startPieceSelectionPhase(game, world, sender);
    }

    /**
     * 진행 중인 게임을 중단합니다.
     *
     * @param sender 중단을 요청한 주체
     */
    @HandleException
    public void stopGame(@NonNull CommandSender sender) {
        Game game = gameRepository.find().orElseThrow(GameException::notFound);

        gameNotifier.stopGuidance();
        gameTaskScheduler.shutdown();
        pieceService.despawnPieces(game.unitPieces());
        teamService.revealEnemies();
        gameRepository.delete();
        gameNotifier.notifyGameStop(sender);
    }

    /**
     * 기물의 상세 정보를 표시합니다.
     *
     * @param player 정보를 확인할 플레이어
     * @param entity 확인할 대상
     */
    @HandleException
    public void inspectPiece(@NonNull Player player, @NonNull Entity entity) {
        Game game = gameRepository.find().orElse(null);

        if (game == null || game.phase() != GamePhase.PIECE_SELECTION) {
            return;
        }

        game.findPiece(entity.getUniqueId())
                .filter(piece -> piece instanceof UnitPiece)
                .map(piece -> (UnitPiece) piece)
                .filter(Piece::isSelectable)
                .filter(unitPiece -> isFriendlyPiece(player, unitPiece))
                .ifPresent(unitPiece -> pieceInfoRenderer.renderInfo(
                        player,
                        unitPiece,
                        game.isPieceSelected(unitPiece.id())
                ));
    }

    /**
     * 플레이어 접속 시 게임 단계에 맞춰 시야를 업데이트합니다.
     *
     * @param player 접속한 플레이어
     */
    @HandleException
    public void updatePlayerVisibility(@NonNull Player player) {
        gameRepository.find().ifPresent(game -> {
            if (game.phase() != GamePhase.PIECE_SELECTION) {
                return;
            }

            teamService.findTeam(player).ifPresent(team -> {
                teamService.concealEnemiesFor(player, team);
                pieceService.concealEnemyPiecesFor(game, player, team);
            });
        });
    }

    private void startPieceSelectionPhase(
            @NonNull Game game,
            @NonNull World world,
            @NonNull CommandSender sender
    ) {
        pieceService.spawnPieces(game.board(), world)
                .thenAccept(spawnedPieces -> {
                    registerSpawnedPieces(spawnedPieces);
                    teleportPlayersToStartingPositions(game, world);
                    teamService.concealEnemies();
                    gameNotifier.announcePieceSelectionStart(teamService.getAllOnlinePlayers());
                    gameNotifier.startPieceSelectionGuidance();
                    schedulePieceSelectionPhaseEnd();
                })
                .exceptionally(t -> {
                    onPieceSpawnFailed(t, sender);

                    return null;
                });
    }

    private void registerSpawnedPieces(@NonNull Map<Coordinate, UnitPiece> spawnedPieces) {
        Game currentGame = gameRepository.find().orElseThrow(GameSystemException::gameStartInterrupted);
        Game updatedGame = currentGame.withPieces(spawnedPieces);

        gameRepository.save(updatedGame);
        pieceService.concealPieces(updatedGame);
    }

    private void onPieceSpawnFailed(@NonNull Throwable t, @NonNull CommandSender sender) {
        Throwable cause = t.getCause() == null ? t : t.getCause();

        if (cause instanceof Exception e) {
            exceptionDispatcher.dispatch(e, sender, "async spawnPieces");
        } else if (cause instanceof Error error) {
            throw error;
        } else {
            throw new RuntimeException("Fatal error in async task", cause);
        }
    }

    private void teleportPlayersToStartingPositions(@NonNull Game game, @NonNull World world) {
        Board board = game.board();
        Vector center = board.center().add(new Vector(0, 1, 0));
        int stepLength = board.squareGrid().squareSpec().height();
        Orientation orientation = board.orientation();
        Location whiteLocation = calculateOffsetLocation(center, orientation.backward(), stepLength, world);
        Location blackLocation = calculateOffsetLocation(center, orientation.forward(), stepLength, world);

        teamService.teleportTeam(TeamColor.WHITE, whiteLocation);
        teamService.teleportTeam(TeamColor.BLACK, blackLocation);
    }

    private void schedulePieceSelectionPhaseEnd() {
        long selectionDurationTicks = 5 * 60 * 20L;

        gameTaskScheduler.scheduleOnce(
                GameTaskType.PHASE_TRANSITION,
                gameNotifier::stopGuidance,
                selectionDurationTicks
        );
    }

    @NonNull
    private Location calculateOffsetLocation(
            @NonNull Vector center,
            @NonNull Vector direction,
            double length,
            @NonNull World world
    ) {
        Vector offset = direction.clone().multiply(length / 2);
        Location location = center.clone().add(offset).toLocation(world);

        location.setDirection(direction);

        return location;
    }

    private boolean isFriendlyPiece(@NonNull Player player, @NonNull Piece piece) {
        return teamService.findTeam(player)
                .map(piece::isTeam)
                .orElse(false);
    }
}
