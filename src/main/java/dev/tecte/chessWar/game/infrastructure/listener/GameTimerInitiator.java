package dev.tecte.chessWar.game.infrastructure.listener;

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
 * 게임 타이머의 생명주기에 따른 표출 시점을 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameTimerInitiator implements Listener {
    private final GameFlowCoordinator flowCoordinator;
    private final GameTimerService timerService;

    /**
     * 기물 선택 단계가 시작되면 타이머를 시작합니다.
     *
     * @param event 기물 선택 시작 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void initiateSelectionTimer(@NonNull GameSelectionStartedEvent event) {
        flowCoordinator.initiatePhaseTimer(event.game().phase(), event.participants().keySet());
    }

    /**
     * 참여자가 다시 합류하면 타이머를 복구합니다.
     *
     * @param event 플레이어 합류 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void restoreTimerForParticipant(@NonNull GameParticipantJoinedEvent event) {
        timerService.restore(event.playerId());
    }
}
