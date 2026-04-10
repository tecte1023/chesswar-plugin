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
 * 게임의 생명주기와 비즈니스 흐름을 관리합니다.
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
     * @param board     체스판 정보
     * @param starterId 행위자 ID
     */
    public void prepareBattlefield(@NonNull Board board, @NonNull UUID starterId) {
        CommandSender starter = userResolver.resolveSender(starterId);

        pieceService.spawnPieces(board, starter)
                .thenAccept(unitPlacements -> {
                    Map<UUID, TeamColor> participants = teamService.findAllParticipants();

                    eventDispatcher.dispatch(PiecesSpawnedEvent.of(
                            board,
                            unitPlacements,
                            participants,
                            starterId
                    ));
                });
    }

    /**
     * 기물 선택 단계를 시작합니다.
     *
     * @param unitPlacements 기물 배치 정보
     * @param participants   참여자 정보
     * @param starterId      행위자 ID
     */
    public void startSelectionPhase(
            @NonNull Map<Coordinate, UnitPiece> unitPlacements,
            @NonNull Map<UUID, TeamColor> participants,
            @NonNull UUID starterId
    ) {
        Game game = gameRepository.find()
                .orElseThrow(() -> GameSystemException.gameTransitionInterrupted(GamePhase.PIECE_SELECTION));

        game = game.startSelection(unitPlacements);
        gameRepository.save(game);
        eventDispatcher.dispatch(GameSelectionStartedEvent.of(
                game,
                participants,
                starterId
        ));
    }

    /**
     * 타이머를 시작합니다.
     *
     * @param phase          게임 단계
     * @param participantIds 참여자 ID 목록
     */
    public void initiatePhaseTimer(
            @NonNull GamePhase phase,
            @NonNull Collection<UUID> participantIds
    ) {
        timerPolicy.findSettings(phase)
                .ifPresent(settings -> timerService.start(phase, settings, participantIds));
    }

    /**
     * 플레이어 참여를 처리합니다.
     *
     * @param player 접속한 플레이어
     */
    public void handlePlayerJoin(@NonNull Player player) {
        teamService.findTeam(player).ifPresent(teamColor ->
                gameRepository.find().ifPresent(game ->
                        eventDispatcher.dispatch(GameParticipantJoinedEvent.of(
                                player.getUniqueId(),
                                teamColor,
                                game.unitPlacements()
                        ))
                )
        );
    }
}
