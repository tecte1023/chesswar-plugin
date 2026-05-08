package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameAnnouncer;
import dev.tecte.chessWar.game.domain.event.GameStoppedEvent;
import dev.tecte.chessWar.port.UserResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 종료 사실의 전역 전파를 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameStopAnnounceListener implements Listener {
    private final GameAnnouncer gameAnnouncer;
    private final UserResolver userResolver;

    /**
     * 모든 참여자에게 게임 종료를 알리고 활성화된 가이드 UI를 제거합니다.
     *
     * @param event 게임 중단 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStopped(@NonNull GameStoppedEvent event) {
        gameAnnouncer.stopGuidance();
        gameAnnouncer.notifyGameStop(userResolver.resolveSender(event.senderId()));
    }
}
