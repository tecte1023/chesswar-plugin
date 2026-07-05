/*
package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.BoardEnvironmentPresenter;
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
    private final BoardEnvironmentPresenter boardEnvPresenter;

    @EventHandler
    public void onReadyClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        final ItemStack cursor = event.getCursor();
        final ItemStack hotbar = (event.getHotbarButton() == -1)
                ? null
                : player.getInventory().getItem(event.getHotbarButton());

        if (!boardEnvPresenter.isReadyButton(item)
            && !boardEnvPresenter.isReadyButton(cursor)
            && !boardEnvPresenter.isReadyButton(hotbar)
        ) {
            return;
        }

        event.setCancelled(true);
        plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);

        if (boardEnvPresenter.isReadyButton(item)) {
            gameManager.handleReadyUp(player, event.getInventory().getLocation());
        }
    }
}
*/
