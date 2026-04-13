package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.event.GameParticipantJoinedEvent;
import dev.tecte.chessWar.game.domain.event.GameSelectionStartedEvent;
import dev.tecte.chessWar.game.domain.event.GameStartedEvent;
import dev.tecte.chessWar.game.domain.event.GameStoppedEvent;
import dev.tecte.chessWar.game.domain.event.PiecesSpawnedEvent;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.exception.GameSystemException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.PhaseTimerSettings;
import dev.tecte.chessWar.game.domain.policy.GamePhaseTimerPolicy;
import dev.tecte.chessWar.piece.application.PieceService;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.port.UserResolver;
import dev.tecte.chessWar.port.WorldResolver;
import dev.tecte.chessWar.team.application.TeamService;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * 게임의 전반적인 진행 흐름을 조율합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameFlowCoordinator {
    private final GamePhaseTimerPolicy timerPolicy;
    private final GameRepository gameRepository;
    private final GameTimerService timerService;
    private final BoardService boardService;
    private final TeamService teamService;
    private final PieceService pieceService;
    private final WorldResolver worldResolver;
    private final UserResolver userResolver;
    private final DomainEventDispatcher eventDispatcher;

    /**
     * 게임을 시작합니다.
     *
     * @param sender 행위자
     * @throws GameException 시작 조건을 충족하지 못했을 경우
     */
    public void startGame(@NonNull CommandSender sender) {
        if (gameRepository.isGameInProgress()) {
            throw GameException.alreadyInProgress();
        }

        Board board = boardService.findBoard().orElseThrow(GameException::boardNotSetup);

        worldResolver.ensureExists(board.worldName(), GameException::worldNotFound);
        teamService.ensureMinimumCapacityMet();

        Game game = Game.create(board);

        gameRepository.save(game);
        eventDispatcher.dispatch(GameStartedEvent.of(
                game,
                userResolver.resolveActorId(sender)
        ));
    }

    /**
     * 게임을 중단합니다.
     *
     * @param sender 행위자
     * @throws GameException 게임을 찾지 못했을 경우
     */
    public void stopGame(@NonNull CommandSender sender) {
        Game game = gameRepository.find().orElseThrow(GameException::notFound);

        gameRepository.delete();
        eventDispatcher.dispatch(GameStoppedEvent.of(
                game.units(),
                userResolver.resolveActorId(sender)
        ));
    }

    /**
     * 전장을 준비합니다.
     *
     * @param board     체스판
     * @param starterId 행위자 ID
     */
    public void prepareBattlefield(@NonNull Board board, @NonNull UUID starterId) {
        CommandSender starter = userResolver.resolveSender(starterId);

        pieceService.spawnPieces(board, starter)
                .thenAccept(unitPlacements -> eventDispatcher.dispatch(
                        PiecesSpawnedEvent.of(
                                board,
                                unitPlacements,
                                teamService.findAllParticipants(),
                                starterId
                        )
                ));
    }

    /**
     * 기물 선택 단계를 시작합니다.
     *
     * @param initialPlacements 초기 기물 배치
     * @param participants      참여자 정보
     * @param starterId         행위자 ID
     * @throws GameSystemException 단계 전이 실패 시
     */
    public void startSelectionPhase(
            @NonNull Map<Coordinate, UnitPiece> initialPlacements,
            @NonNull Map<UUID, TeamColor> participants,
            @NonNull UUID starterId
    ) {
        Game game = gameRepository.find()
                .orElseThrow(() -> GameSystemException.gameTransitionInterrupted(GamePhase.PIECE_SELECTION));
        PhaseTimerSettings timerSettings = timerPolicy.findSettings(GamePhase.PIECE_SELECTION)
                .orElseThrow(() -> GameSystemException.gameTransitionInterrupted(GamePhase.PIECE_SELECTION));

        game = game.startSelection(initialPlacements, timerSettings);
        gameRepository.save(game);
        eventDispatcher.dispatch(GameSelectionStartedEvent.of(
                game,
                participants,
                starterId
        ));
    }

    /**
     * 게임 단계에 맞는 타이머를 활성화합니다.
     *
     * @param game           대상 게임
     * @param participantIds 참여자 ID 목록
     */
    public void initiatePhaseTimer(@NonNull Game game, @NonNull Collection<UUID> participantIds) {
        game.timedState()
                .ifPresent(timedState -> timerService.start(game.phase(), timedState, participantIds));
    }

    /**
     * 진행 중인 게임의 타이머를 복구합니다.
     */
    public void resumeActiveGameTimer() {
        gameRepository.find()
                .ifPresent(game -> initiatePhaseTimer(game, teamService.findAllParticipantIds()));
    }

    /**
     * 플레이어 참여를 처리합니다.
     *
     * @param player 접속한 플레이어
     */
    public void handlePlayerJoin(@NonNull Player player) {
        teamService.findTeam(player)
                .flatMap(teamColor -> gameRepository.find()
                        .map(game -> GameParticipantJoinedEvent.of(
                                player.getUniqueId(),
                                teamColor,
                                game.unitPlacements()
                        )))
                .ifPresent(eventDispatcher::dispatch);
    }
}
