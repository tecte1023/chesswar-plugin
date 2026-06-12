package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.ChessWar;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@UtilityClass
public class ConsumableItemUtils {
    private static final NamespacedKey ITEM_ID_KEY =
            new NamespacedKey(JavaPlugin.getPlugin(ChessWar.class), "consumable_id");

    public static final String ID_LEAP = "leap";

    public static ItemStack createLeapItem() {
        final ItemStack item = new ItemStack(Material.FEATHER);
        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("도약", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            
            meta.lore(List.of(
                    Component.text(""),
                    Component.text("▶ 사용 시 이번 턴에 한해 아군 기물 하나를", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("  뛰어넘어 이동할 수 있습니다.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text(""),
                    Component.text("▶ 우클릭하여 사용", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
            ));

            meta.getPersistentDataContainer().set(ITEM_ID_KEY, PersistentDataType.STRING, ID_LEAP);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static String getConsumableId(final ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(ITEM_ID_KEY, PersistentDataType.STRING);
    }
}
