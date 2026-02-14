package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameNotifier;
import dev.tecte.chessWar.game.domain.event.GameStoppedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 종료 소식을 플레이어들에게 알립니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameStopNotificationListener implements Listener {
    private final GameNotifier gameNotifier;

    /**
     * 가이드 UI를 제거하고 게임 종료 메시지를 전송합니다.
     *
     * @param event 게임 종료 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void notifyPlayers(@NonNull GameStoppedEvent event) {
        gameNotifier.stopGuidance();
        gameNotifier.notifyGameStop(event.stopper());
    }
}
