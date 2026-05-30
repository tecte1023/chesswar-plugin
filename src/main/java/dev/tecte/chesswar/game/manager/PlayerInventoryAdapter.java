package dev.tecte.chesswar.game.manager;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class PlayerInventoryAdapter {
    private final NamespacedKey orderKey;

    public PlayerInventoryAdapter(final Plugin plugin, final String keyName) {
        orderKey = new NamespacedKey(plugin, keyName);
    }

    public Optional<Integer> extractTurnOrder(final Player player) {
        if (player == null) return Optional.empty();

        int bestOrder = Integer.MAX_VALUE;
        boolean found = false;

        for (final ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                final Integer order = item.getItemMeta()
                        .getPersistentDataContainer()
                        .get(orderKey, PersistentDataType.INTEGER);

                if (order != null && order < bestOrder) {
                    bestOrder = order;
                    found = true;
                }
            }
        }

        return found ? Optional.of(bestOrder) : Optional.empty();
    }

    public void clearOrderItems(final Player player) {
        if (player == null) return;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            final ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.hasItemMeta()) {
                if (item.getItemMeta().getPersistentDataContainer().has(orderKey, PersistentDataType.INTEGER)) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }
}
