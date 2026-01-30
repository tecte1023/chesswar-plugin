package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 게임 진행 상태에 따라 플레이어의 시야를 제어합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameVisibilityListener implements Listener {
    private final GameService gameService;

    /**
     * 플레이어 접속 시 게임 상태에 따른 시야 처리를 요청합니다.
     *
     * @param event 플레이어 접속 이벤트
     */
    @EventHandler
    public void onPlayerJoin(@NonNull PlayerJoinEvent event) {
        gameService.updatePlayerVisibility(event.getPlayer());
    }
}
