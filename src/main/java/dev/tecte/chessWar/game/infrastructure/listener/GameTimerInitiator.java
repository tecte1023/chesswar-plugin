package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameFlowCoordinator;
import dev.tecte.chessWar.game.domain.event.GameSelectionStartedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 타이머의 시작 시점을 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameTimerInitiator implements Listener {
    private final GameFlowCoordinator flowCoordinator;

    /**
     * 기물 선택 단계가 시작되면 타이머를 시작합니다.
     *
     * @param event 기물 선택 시작 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void initiateSelectionTimer(@NonNull GameSelectionStartedEvent event) {
        flowCoordinator.initiatePhaseTimer(event.game().phase(), event.participants().keySet());
    }
}
