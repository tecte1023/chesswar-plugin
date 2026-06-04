package dev.tecte.chesswar.game.listener;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class GamePieceLifecycleListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onPieceRemove(final EntityRemoveFromWorldEvent event) {
        gameManager.handlePieceDisappearance(event.getEntity());
    }

    @EventHandler
    public void onPieceDeath(final EntityDeathEvent event) {
        gameManager.handlePieceDisappearance(event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        gameManager.handlePieceDisappearance(event.getPlayer());
    }
}
