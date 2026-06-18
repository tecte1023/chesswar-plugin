package dev.tecte.chesswar.game.manager;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class PlayerInventoryAdapter {
    private final NamespacedKey orderKey;

    public PlayerInventoryAdapter(final Plugin plugin, final String keyName) {
        orderKey = new NamespacedKey(plugin, keyName);
    }

    public int extractTurnOrder(final Player player) {
        if (player == null) {
            return -1;
        }

        final PlayerInventory inv = player.getInventory();
        final int size = inv.getSize();

        for (int i = 0; i < size; i++) {
            final int order = getTurnOrder(inv.getItem(i));

            if (order != -1) {
                return order;
            }
        }

        return -1;
    }

    public void clearOrderItems(final Player player) {
        if (player == null) {
            return;
        }

        final PlayerInventory inv = player.getInventory();
        final int size = inv.getSize();

        for (int i = 0; i < size; i++) {
            if (getTurnOrder(inv.getItem(i)) == -1) {
                continue;
            }

            inv.setItem(i, null);
        }
    }

    public int consumeTurnOrder(final Player player) {
        if (player == null) {
            return -1;
        }

        int firstFoundOrder = -1;
        final PlayerInventory inv = player.getInventory();
        final int size = inv.getSize();

        for (int i = 0; i < size; i++) {
            final int order = getTurnOrder(inv.getItem(i));

            if (order != -1) {
                if (firstFoundOrder == -1) {
                    firstFoundOrder = order;
                }

                inv.setItem(i, null);
            }
        }

        return firstFoundOrder;
    }

    private int getTurnOrder(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return -1;
        }

        final ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return -1;
        }

        final Integer order = meta.getPersistentDataContainer().get(orderKey, PersistentDataType.INTEGER);

        return order == null ? -1 : order;
    }
}
