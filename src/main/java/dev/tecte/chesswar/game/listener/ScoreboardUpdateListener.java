package dev.tecte.chesswar.game.listener;

import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.ScoreboardManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class ScoreboardUpdateListener implements Listener {
    private final ScoreboardManager scoreboardManager;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scoreboardManager.updatePlayer(event.getPlayer());
        scoreboardManager.updateAll();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        scoreboardManager.remove(event.getPlayer());
        scoreboardManager.updateAll();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 순서 조율 단계에서 아이템 위치 변경 시 스코어보드 갱신
        scoreboardManager.updateAll();
    }
}
