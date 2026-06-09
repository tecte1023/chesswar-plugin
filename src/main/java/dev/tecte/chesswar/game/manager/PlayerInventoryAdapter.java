package dev.tecte.chesswar.game.manager;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class PlayerInventoryAdapter {
    private final NamespacedKey orderKey;

    public PlayerInventoryAdapter(final Plugin plugin, final String keyName) {
        orderKey = new NamespacedKey(plugin, keyName);
    }

    public Optional<Integer> extractTurnOrder(final Player player) {
        if (player == null) {
            return Optional.empty();
        }

        // 인벤토리 슬롯 순서대로 탐색 (0번 슬롯부터)
        for (final ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) {
                continue;
            }

            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }

            final Integer order = meta.getPersistentDataContainer().get(orderKey, PersistentDataType.INTEGER);
            if (order != null) {
                return Optional.of(order);
            }
        }

        return Optional.empty();
    }

    public void clearOrderItems(final Player player) {
        if (player == null) {
            return;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            final ItemStack item = player.getInventory().getItem(i);

            if (item == null || !item.hasItemMeta()) {
                continue;
            }

            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }

            if (meta.getPersistentDataContainer().has(orderKey, PersistentDataType.INTEGER)) {
                player.getInventory().setItem(i, null);
            }
        }
    }
}
