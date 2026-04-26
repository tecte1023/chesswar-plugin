package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameFlowCoordinator;
import dev.tecte.chessWar.game.domain.event.AllParticipantsSelectedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 기물 선택 완료 시 타이머 단축을 담당합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SelectionTimerAccelerator implements Listener {
    private final GameFlowCoordinator flowCoordinator;

    /**
     * 기물 선택 완료 시 타이머를 단축합니다.
     *
     * @param event 기물 선택 완료 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAllParticipantsSelected(@NonNull AllParticipantsSelectedEvent event) {
        flowCoordinator.accelerateSelectionTimer();
    }
}
