package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.ChessWar;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@UtilityClass
public class PieceItemUtils {
    private static final NamespacedKey PIECE_TYPE_KEY =
            new NamespacedKey(JavaPlugin.getPlugin(ChessWar.class), "piece_type");

    public static ItemStack createPieceItem(final PieceType type) {
        final Material material = switch (type) {
            case KING -> Material.DIAMOND_SWORD;
            case QUEEN -> Material.NETHERITE_SWORD;
            case ROOK -> Material.IRON_AXE;
            case KNIGHT -> Material.IRON_SWORD;
            case BISHOP -> Material.BLAZE_ROD;
            case PAWN -> Material.STONE_HOE;
        };
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(type.displayName() + "의 무기", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text(""),
                    Component.text("▶ 상점 열기 (F)", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
            ));

            meta.getPersistentDataContainer().set(PIECE_TYPE_KEY, PersistentDataType.STRING, type.name());

            if (type == PieceType.BISHOP || type == PieceType.QUEEN) {
                final AttributeModifier rangeModifier = new AttributeModifier(
                        new NamespacedKey(JavaPlugin.getPlugin(ChessWar.class), "ranged_interaction"),
                        24.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND
                );
                meta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, rangeModifier);
            }

            final AttributeModifier modifier = new AttributeModifier(
                    new NamespacedKey(JavaPlugin.getPlugin(ChessWar.class), "no_attack_cooldown"),
                    2048.0,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND
            );

            meta.addAttributeModifier(Attribute.ATTACK_SPEED, modifier);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static boolean isPieceItem(final ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(PIECE_TYPE_KEY, PersistentDataType.STRING);
    }

    public static PieceType getPieceType(final ItemStack item) {
        if (!isPieceItem(item)) {
            return null;
        }

        final ItemMeta meta = item.getItemMeta();
        final String typeName = meta.getPersistentDataContainer().get(PIECE_TYPE_KEY, PersistentDataType.STRING);

        try {
            return PieceType.valueOf(typeName);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    public static void replacePlayerPieceItem(final Player player, final PieceType type) {
        removePlayerPieceItems(player);
        player.getInventory().addItem(createPieceItem(type));
    }

    public static void removePlayerPieceItems(final Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            final ItemStack item = player.getInventory().getItem(i);
            if (isPieceItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }
    }
}
