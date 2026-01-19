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
import dev.tecte.chessWar.piece.domain.model.PieceType;
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
 * 게임의 생명주기와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameService {
    private static final int MIN_PLAYERS = 1;
    private static final long PIECE_SELECTION_DURATION_TICKS = 5 * 60 * 20;

    private final GameNotifier gameNotifier;
    private final BoardService boardService;
    private final TeamService teamService;
    private final PieceService pieceService;
    private final GameRepository gameRepository;
    private final GameTaskScheduler gameTaskScheduler;
    private final PieceInfoRenderer pieceInfoRenderer;
    private final ExceptionDispatcher exceptionDispatcher;

    /**
     * 게임 시작 조건을 확인한 후, 조건을 충족하면 게임을 시작합니다.
     *
     * @param sender 명령어를 실행한 주체
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

        if (!teamService.areAllTeamsReadyToStart(MIN_PLAYERS)) {
            throw GameException.insufficientPlayers(MIN_PLAYERS);
        }

        Game game = Game.create(board);

        gameRepository.save(game);
        startPieceSelectionPhase(game, world, sender);
    }

    /**
     * 게임을 중단하고 모든 데이터를 초기화합니다.
     * 소환된 기물들도 모두 제거됩니다.
     *
     * @param sender 명령어를 실행한 주체
     */
    @HandleException
    public void stopGame(@NonNull CommandSender sender) {
        Game game = gameRepository.find().orElseThrow(GameException::notFound);

        gameNotifier.stopGuidance();
        gameTaskScheduler.shutdown();
        pieceService.despawnPieces(game);
        teamService.revealEnemies();
        gameRepository.delete();
        gameNotifier.notifyGameStop(sender);
    }

    /**
     * 대상 엔티티를 검사하고, 조건이 충족되면 상세 정보를 표시합니다.
     * <p>
     * <b>직업 선택 단계</b>이며 자신의 팀 기물인 경우, 해당 기물의 상세 정보를 보여줍니다. (단, 폰은 제외됩니다.)
     *
     * @param player 정보를 표시할 대상 플레이어
     * @param entity 검사 대상 엔티티
     */
    @HandleException
    public void inspectPiece(@NonNull Player player, @NonNull Entity entity) {
        gameRepository.find()
                .filter(game -> game.phase() == GamePhase.PIECE_SELECTION)
                .flatMap(game -> game.findPiece(entity.getUniqueId()))
                .filter(piece -> piece.spec().type() != PieceType.PAWN)
                .filter(piece -> isFriendlyPiece(player, piece))
                .ifPresent(piece -> pieceInfoRenderer.renderInfo(player, piece));
    }

    private void startPieceSelectionPhase(
            @NonNull Game game,
            @NonNull World world,
            @NonNull CommandSender sender
    ) {
        pieceService.spawnPieces(game.board(), world)
                .thenAccept(this::registerSpawnedPieces)
                .exceptionally(t -> {
                    onPieceSpawnFailed(t, sender);

                    return null;
                });
        teleportPlayersToStartingPositions(game, world);
        teamService.concealEnemies();
        gameNotifier.announcePieceSelectionStart(teamService.getAllOnlinePlayers());
        gameNotifier.startPieceSelectionGuidance();
        schedulePieceSelectionPhaseEnd();
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
        gameTaskScheduler.scheduleOnce(GameTaskType.PHASE_TRANSITION, () -> {
            gameNotifier.stopGuidance();
            startTurnOrderSelectionPhase();
        }, PIECE_SELECTION_DURATION_TICKS);
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

    private void startTurnOrderSelectionPhase() {
        Game game = gameRepository.find()
                .orElseThrow(() -> GameSystemException.gameTransitionInterrupted(GamePhase.TURN_ORDER_SELECTION));
        Game turnOrderSelectionPhaseGame = game.startTurnSelection();

        gameRepository.save(turnOrderSelectionPhaseGame);
        teamService.revealEnemies();
        pieceService.revealPieces(turnOrderSelectionPhaseGame);
    }

    private boolean isFriendlyPiece(@NonNull Player player, @NonNull UnitPiece piece) {
        return teamService.findTeam(player)
                .map(team -> team == piece.spec().teamColor())
                .orElse(false);
    }
}
