package dev.tecte.chesswar.game.listener;

import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@RequiredArgsConstructor
public class GameStartListener implements Listener {
    private final GameManager gameManager;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (gameManager.isBindingAdmin(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            gameManager.bindStartButton(event.getPlayer(), event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        if (gameManager.isStartButton(event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            gameManager.startGame(event.getPlayer());
        }
    }
}
