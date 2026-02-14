package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.port.GameTaskScheduler;
import dev.tecte.chessWar.game.domain.event.GameStoppedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 종료 시점에 예약된 태스크들을 정리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameTaskCleanupListener implements Listener {
    private final GameTaskScheduler gameTaskScheduler;

    /**
     * 진행 중인 모든 게임 태스크를 종료하여 추가적인 상태 변화를 차단합니다.
     *
     * @param event 게임 종료 이벤트
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void cleanupTasks(@NonNull GameStoppedEvent event) {
        gameTaskScheduler.shutdown();
    }
}
