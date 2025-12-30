package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.exception.GameNotFoundException;
import dev.tecte.chessWar.game.domain.exception.GameStartConditionException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.UnitPiece;
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
    private static final long POST_GAME_DURATION_TICKS = 10 * 20;

    private final PieceService pieceService;
    private final GameNotifier gameNotifier;
    private final GameTaskScheduler gameTaskScheduler;
    private final BoardService boardService;
    private final TeamService teamService;
    private final GameRepository gameRepository;
    private final ExceptionDispatcher exceptionDispatcher;

    /**
     * 게임 시작 조건을 확인한 후, 조건을 충족하면 게임을 시작합니다.
     *
     * @param sender 명령어를 실행한 주체
     * @throws GameStartConditionException 게임 시작 조건을 충족하지 못했을 경우
     */
    @HandleException
    public void startGame(@NonNull CommandSender sender) {
        if (gameRepository.isGameInProgress()) {
            throw GameStartConditionException.forGameAlreadyInProgress();
        }

        Board board = boardService.findBoard().orElseThrow(GameStartConditionException::forBoardNotExists);
        String worldName = board.worldName();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw GameStartConditionException.forWorldNotFound(worldName);
        }

        if (!teamService.areAllTeamsReadyToStart(MIN_PLAYERS)) {
            throw GameStartConditionException.forTeamsNotReady(MIN_PLAYERS);
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
        Game game = gameRepository.find().orElseThrow(GameNotFoundException::noGameInProgress);

        gameNotifier.stopGuidance();
        gameTaskScheduler.shutdown();
        pieceService.despawnPieces(game);
        teamService.revealEnemies();
        gameRepository.delete();
        gameNotifier.notifyGameStop(sender);
    }

    private void startPieceSelectionPhase(
            @NonNull Game game,
            @NonNull World world,
            @NonNull CommandSender sender
    ) {
        pieceService.spawnPieces(world, game.board())
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
        Game currentGame = gameRepository.find().orElseThrow(GameNotFoundException::duringPieceSpawning);
        Game updatedGame = currentGame.withPieces(spawnedPieces);

        gameRepository.save(updatedGame);
        pieceService.concealPieces(updatedGame);
    }

    private void onPieceSpawnFailed(@NonNull Throwable t, @NonNull CommandSender sender) {
        Throwable cause = t.getCause() == null ? t : t.getCause();

        // endGame();

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
                .orElseThrow(() -> GameNotFoundException.duringPhaseTransition(GamePhase.TURN_ORDER_SELECTION));
        Game turnOrderSelectionPhaseGame = game.startTurnSelection();

        gameRepository.save(turnOrderSelectionPhaseGame);
        teamService.revealEnemies();
        pieceService.revealPieces(turnOrderSelectionPhaseGame);

//        scheduler.runTaskLater(plugin, () -> startBattlePhase(turnOrderSelectionPhaseGame, TeamColor.WHITE), 20L);
    }

    private void startBattlePhase(@NonNull Game game, @NonNull TeamColor startingTeam) {
        Game battlePhaseGame = game.startBattle(startingTeam);
        gameRepository.save(battlePhaseGame);
        log.info("Game phase changed to BATTLE. Starting turn: {}", startingTeam);

        String worldName = battlePhaseGame.board().worldName();
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            log.error("Failed to start battle phase: World '{}' not found.", worldName);
            endGame();
            return;
        }

        CommandSender console = Bukkit.getConsoleSender();
        pieceService.spawnPieces(world, battlePhaseGame.board())
                .thenAccept(spawnedPieces -> {
                    gameRepository.find().ifPresent(currentGame -> {
                        Game updatedGame = currentGame.withPieces(spawnedPieces);
                        gameRepository.save(updatedGame);
                        log.info("전투 단계 기물 재소환 및 게임 데이터 업데이트 완료.");
                    });
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof Exception e) {
                        exceptionDispatcher.dispatch(e, console, "async battle re-spawnPieces");
                    } else {
                        log.error("Critical Error (Non-Exception) during async battle re-spawnPieces", cause);
                    }
                    endGame();
                    return null;
                });
    }

    /**
     * 게임을 종료합니다.
     * 현재 진행 중인 게임이 있다면 {@link GamePhase#ENDED} 상태로 변경하고, 일정 시간 후 데이터를 정리합니다.
     */
    public void endGame() {
        Game game = gameRepository.find().orElse(null);
        if (game == null) {
            return;
        }

        if (game.phase() != GamePhase.ENDED) {
            gameRepository.save(game.end());
            log.info("Game phase changed to ENDED.");
        }

        gameTaskScheduler.scheduleOnce(GameTaskType.GAME_CLEANUP, this::cleanupAfterGameEnd, POST_GAME_DURATION_TICKS);
    }

    private void cleanupAfterGameEnd() {
        if (gameRepository.find().isEmpty()) return;

        gameRepository.delete();
    }
}
