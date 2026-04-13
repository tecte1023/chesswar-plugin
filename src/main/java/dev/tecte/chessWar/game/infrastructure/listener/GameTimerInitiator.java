package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.common.event.ChessWarStartedEvent;
import dev.tecte.chessWar.game.application.GameFlowCoordinator;
import dev.tecte.chessWar.game.application.GameTimerService;
import dev.tecte.chessWar.game.domain.event.GameParticipantJoinedEvent;
import dev.tecte.chessWar.game.domain.event.GameSelectionStartedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 이벤트에 따른 타이머 활성화 및 복구 시점을 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameTimerInitiator implements Listener {
    private final GameFlowCoordinator flowCoordinator;
    private final GameTimerService timerService;

    /**
     * 진행 중인 게임의 타이머를 복구합니다.
     *
     * @param event 서버 시작 이벤트
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void resumeTimersOnStart(@NonNull ChessWarStartedEvent event) {
        flowCoordinator.resumeActiveGameTimer();
    }

    /**
     * 기물 선택 단계의 타이머를 활성화합니다.
     *
     * @param event 기물 선택 시작 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void initiateSelectionTimer(@NonNull GameSelectionStartedEvent event) {
        flowCoordinator.initiatePhaseTimer(event.game(), event.participants().keySet());
    }

    /**
     * 참여자의 타이머 표시를 복구합니다.
     *
     * @param event 플레이어 합류 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void restoreTimerForParticipant(@NonNull GameParticipantJoinedEvent event) {
        timerService.restore(event.playerId());
    }
}
