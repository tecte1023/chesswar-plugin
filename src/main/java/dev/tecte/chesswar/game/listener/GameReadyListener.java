package dev.tecte.chesswar.game.listener;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class GameReadyListener implements Listener {
    private final Plugin plugin;
    private final GameManager gameManager;
    private final BoardManager boardManager;

    @EventHandler
    public void onReadyClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        final ItemStack cursor = event.getCursor();
        final ItemStack hotbar = (event.getHotbarButton() == -1)
                ? null
                : player.getInventory().getItem(event.getHotbarButton());

        if (!boardManager.isReadyButton(item)
            && !boardManager.isReadyButton(cursor)
            && !boardManager.isReadyButton(hotbar)
        ) {
            return;
        }

        event.setCancelled(true);
        plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);

        if (boardManager.isReadyButton(item)) {
            gameManager.handleReadyUp(player, event.getInventory().getLocation());
        }
    }
}
