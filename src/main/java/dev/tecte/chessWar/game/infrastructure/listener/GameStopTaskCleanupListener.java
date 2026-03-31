package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.game.domain.event.GameStoppedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 종료 시 시스템 자원의 안전한 회수를 보장합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameStopTaskCleanupListener implements Listener {
    private final GameTaskManager gameTaskManager;

    /**
     * 진행 중인 모든 비동기 작업을 중단하여 추가적인 상태 변화를 방지합니다.
     *
     * @param event 게임 중단 이벤트
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameStopped(@NonNull GameStoppedEvent event) {
        gameTaskManager.shutdown();
    }
}
