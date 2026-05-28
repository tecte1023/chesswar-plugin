package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class GameReadyListener implements Listener {
    private final ChessWar plugin;
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final TimerManager timerManager;

    @EventHandler
    public void onReadyClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        ItemStack hotbar = null;
        Player player = (Player) event.getWhoClicked();

        if (event.getHotbarButton() != -1) {
            hotbar = player.getInventory().getItem(event.getHotbarButton());
        }

        if (isReadyButton(item) || isReadyButton(cursor) || isReadyButton(hotbar)) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);

            if (isReadyButton(item)) {
                Location invLoc = event.getInventory().getLocation();
                if (invLoc != null && !isMyTeamBarracksChest(player, invLoc)) {
                    player.sendMessage(Component.text("자신의 팀 막사에 있는 상자에서만 준비를 완료할 수 있습니다!", NamedTextColor.RED));
                    return;
                }
                gameManager.handleReadyUp(player, timerManager, plugin);
            }
        }
    }

    private boolean isMyTeamBarracksChest(Player player, Location location) {
        return gameManager.findParticipant(player.getUniqueId())
                .map(p -> boardManager.isTeamChest(location, p.team()))
                .orElse(false);
    }

    private boolean isReadyButton(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        NamespacedKey readyKey = new NamespacedKey(plugin, "ready_button");

        return item.getItemMeta().getPersistentDataContainer().has(readyKey, PersistentDataType.BYTE);
    }
}
