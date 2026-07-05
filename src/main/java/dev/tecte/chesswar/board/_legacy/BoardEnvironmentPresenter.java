/*
package dev.tecte.chesswar.board;

import dev.tecte.chesswar.piece.Coordinate;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BoardEnvironmentPresenter {
    public static final String TURN_ORDER_KEY = "turn_order";
    public static final String READY_BUTTON_KEY = "ready_button";
    private static final double PLAYER_DEPLOY_Y_OFFSET = 1.0;
    private static final int READY_BTN_SLOT_54 = 49;
    private static final int READY_BTN_SLOT_27 = 22;

    private final NamespacedKey readyKey;
    private final NamespacedKey orderKey;
    private final BoardComponent boardComponent;

    public void setupTurnOrderChests(final int whiteCount, final int blackCount) {
        if (boardComponent.isEmpty()) return;

        final Board board = boardComponent.board();
        setupReadyChest(board.getBarracks(Team.WHITE), whiteCount);
        setupReadyChest(board.getBarracks(Team.BLACK), blackCount);
    }

    private void setupReadyChest(final Barracks barracks, final int participantCount) {
        final Location b1 = barracks.leftChestLocation();
        final Location b2 = barracks.rightChestLocation();

        b1.getBlock().setType(Material.CHEST, false);
        b2.getBlock().setType(Material.CHEST, false);

        final BlockState state1 = b1.getBlock().getState();
        final BlockState state2 = b2.getBlock().getState();

        final Chest data1 = (Chest) state1.getBlockData();
        final Chest data2 = (Chest) state2.getBlockData();

        data1.setFacing(barracks.chestFacing());
        data2.setFacing(barracks.chestFacing());

        if (barracks.team() == Team.WHITE) {
            data1.setType(Chest.Type.RIGHT);
            data2.setType(Chest.Type.LEFT);
        } else {
            data1.setType(Chest.Type.LEFT);
            data2.setType(Chest.Type.RIGHT);
        }

        state1.setBlockData(data1);
        state2.setBlockData(data2);

        state1.update(true, false);
        state2.update(true, false);

        final boolean b1IsTop = b1.getBlockX() < b2.getBlockX() || (b1.getBlockX() == b2.getBlockX() && b1.getBlockZ() < b2.getBlockZ());
        final Inventory chestInv = ((InventoryHolder) (b1IsTop ? b1 : b2).getBlock().getState()).getInventory();

        chestInv.clear();

        if (participantCount > 0) {
            for (int i = 1; i <= participantCount; i++) {
                var item = new ItemStack(Material.PAPER);
                final ItemMeta meta = item.getItemMeta();

                meta.displayName(Component.text(i + "번 순서", NamedTextColor.GOLD, TextDecoration.BOLD));
                meta.getPersistentDataContainer().set(orderKey, PersistentDataType.INTEGER, i);
                item.setItemMeta(meta);
                chestInv.addItem(item);
            }
        }

        var readyBtn = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        final ItemMeta readyMeta = readyBtn.getItemMeta();

        readyMeta.displayName(Component.text("[ 준비 완료 ]", NamedTextColor.GREEN, TextDecoration.BOLD));
        readyMeta.getPersistentDataContainer().set(readyKey, PersistentDataType.BYTE, (byte) 1);
        readyBtn.setItemMeta(readyMeta);

        final int readySlot = (chestInv.getSize() == 54) ? READY_BTN_SLOT_54 : READY_BTN_SLOT_27;
        chestInv.setItem(readySlot, readyBtn);
    }

    public void disableReadyButton(final Team team) {
        if (boardComponent.isEmpty()) return;

        final Barracks barracks = boardComponent.board().getBarracks(team);
        if (barracks == null) return;

        final Location b1 = barracks.leftChestLocation();
        final Location b2 = barracks.rightChestLocation();

        final boolean b1IsTop = b1.getBlockX() < b2.getBlockX() || (b1.getBlockX() == b2.getBlockX() && b1.getBlockZ() < b2.getBlockZ());
        final org.bukkit.block.Block chestBlock = (b1IsTop ? b1 : b2).getBlock();

        if (!(chestBlock.getState() instanceof InventoryHolder holder)) return;

        final Inventory chestInv = holder.getInventory();
        final int readySlot = (chestInv.getSize() == 54) ? READY_BTN_SLOT_54 : READY_BTN_SLOT_27;

        var disabledBtn = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        final ItemMeta meta = disabledBtn.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("[ 준비 완료 ]", NamedTextColor.GRAY, TextDecoration.BOLD));
            meta.getPersistentDataContainer().set(readyKey, PersistentDataType.BYTE, (byte) 1);
            disabledBtn.setItemMeta(meta);
        }

        chestInv.setItem(readySlot, disabledBtn);
    }

    public boolean isReadyButton(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer().has(readyKey, PersistentDataType.BYTE);
    }

    public void teleportToBarracks(final Team team, final Player player) {
        if (boardComponent.isEmpty()) return;

        final Barracks barracks = boardComponent.board().getBarracks(team);
        if (barracks != null) {
            player.teleport(barracks.spawnLocation());
        }
    }

    public void deployToBattlefield(final Team team, final Coordinate coordinate, final Player player) {
        if (boardComponent.isEmpty()) return;

        final Board board = boardComponent.board();
        final Location spawnLocation = board.getCenterAt(coordinate.x(), coordinate.y()).add(0, PLAYER_DEPLOY_Y_OFFSET, 0);

        if (team == Team.WHITE) {
            spawnLocation.setDirection(board.grid().forward().getDirection());
        } else {
            spawnLocation.setDirection(board.grid().forward().getDirection().multiply(-1));
        }

        player.teleport(spawnLocation);
    }


    public void clearBarracksChests() {
        if (boardComponent.isEmpty()) return;

        for (final Team team : Team.values()) {
            final Barracks barracks = boardComponent.board().getBarracks(team);
            if (barracks != null) {
                clearChestAt(barracks.leftChestLocation());
                clearChestAt(barracks.rightChestLocation());
            }
        }
    }

    private void clearChestAt(final Location loc) {
        final org.bukkit.block.Block block = loc.getBlock();
        if (block.getState() instanceof InventoryHolder holder) {
            final Inventory inv = holder.getInventory();
            final List<HumanEntity> viewers = new ArrayList<>(inv.getViewers());
            for (final HumanEntity viewer : viewers) {
                viewer.closeInventory();
            }
            inv.clear();
        }
        block.setType(Material.AIR);
    }
}
*/
