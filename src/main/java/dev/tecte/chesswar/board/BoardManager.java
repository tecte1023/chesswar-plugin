package dev.tecte.chesswar.board;

import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Getter
@Accessors(fluent = true)
public class BoardManager {
    public static final String TURN_ORDER_KEY = "turn_order";
    public static final String READY_BUTTON_KEY = "ready_button";

    private final NamespacedKey readyKey;
    private final NamespacedKey orderKey;

    private ChessBoard currentBoard;
    private final Map<Team, Barracks> barracksMap = new HashMap<>();
    private final Set<Location> barracksChests = new HashSet<>();
    private final Map<Location, Team> chestTeamOwnership = new HashMap<>();

    public BoardManager(Plugin plugin) {
        this.readyKey = new NamespacedKey(plugin, READY_BUTTON_KEY);
        this.orderKey = new NamespacedKey(plugin, TURN_ORDER_KEY);
    }

    public boolean hasBoard() {
        return currentBoard != null;
    }

    public void currentBoard(ChessBoard board) {
        if (currentBoard != null) {
            log.info("새로운 체스판이 설정되어 기존 체스판의 데이터를 덮어씁니다.");
        }

        currentBoard = board;
        barracksMap.put(Team.WHITE, new Barracks(Team.WHITE, board));
        barracksMap.put(Team.BLACK, new Barracks(Team.BLACK, board));
    }

    public void setupBarracks(PieceManager pieceManager) {
        if (!hasBoard()) return;

        PieceType[] backRow = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (Team team : Team.values()) {
            Barracks barracks = barracksMap.get(team);
            int y = (team == Team.WHITE) ? 0 : 7;

            for (int x = 0; x < 8; x++) {
                pieceManager.spawnPiece(
                        barracks.board().toCenterLocation(Coordinate.of(x, y)),
                        backRow[x],
                        team,
                        Coordinate.of(x, y),
                        barracks.board().forward().getDirection().multiply(team == Team.WHITE ? 1 : -1),
                        true
                );
            }
        }
    }

    public void setupTurnOrderChests(int whiteCount, int blackCount) {
        if (!hasBoard()) return;

        setupReadyChest(barracksMap.get(Team.WHITE), whiteCount);
        setupReadyChest(barracksMap.get(Team.BLACK), blackCount);
    }

    private void setupReadyChest(Barracks barracks, int participantCount) {
        // UI Layout Constants (Local Scope)
        final int readyBtnSlot54 = 49;
        final int readyBtnSlot27 = 22;

        Location b1 = barracks.chestLocation1();
        Location b2 = barracks.chestLocation2();

        b1.getBlock().setType(Material.CHEST, false);
        b2.getBlock().setType(Material.CHEST, false);

        BlockState state1 = b1.getBlock().getState();
        BlockState state2 = b2.getBlock().getState();

        Chest data1 = (Chest) state1.getBlockData();
        Chest data2 = (Chest) state2.getBlockData();

        data1.setFacing(barracks.chestFacing());
        data1.setType(Chest.Type.RIGHT);
        data2.setFacing(barracks.chestFacing());
        data2.setType(Chest.Type.LEFT);

        state1.setBlockData(data1);
        state2.setBlockData(data2);

        state1.update(true, false);
        state2.update(true, false);

        addBarracksChest(b1, barracks.team());
        addBarracksChest(b2, barracks.team());

        boolean b1IsTop = b1.getBlockX() < b2.getBlockX() || (b1.getBlockX() == b2.getBlockX() && b1.getBlockZ() < b2.getBlockZ());
        Inventory chestInv = ((InventoryHolder) (b1IsTop ? b1 : b2).getBlock().getState()).getInventory();

        chestInv.clear();

        if (participantCount > 0) {
            for (int i = 1; i <= participantCount; i++) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(Component.text(i + "번 순서", NamedTextColor.GOLD, TextDecoration.BOLD));
                meta.getPersistentDataContainer().set(orderKey, PersistentDataType.INTEGER, i);
                item.setItemMeta(meta);
                chestInv.addItem(item);
            }
        }

        ItemStack readyBtn = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta readyMeta = readyBtn.getItemMeta();

        readyMeta.displayName(Component.text("[ 준비 완료 ]", NamedTextColor.GREEN, TextDecoration.BOLD));
        readyMeta.getPersistentDataContainer().set(readyKey, PersistentDataType.BYTE, (byte) 1);
        readyBtn.setItemMeta(readyMeta);

        int readySlot = (chestInv.getSize() == 54) ? readyBtnSlot54 : readyBtnSlot27;
        chestInv.setItem(readySlot, readyBtn);
    }

    public boolean isReadyButton(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer().has(readyKey, PersistentDataType.BYTE);
    }

    public void teleportToBarracks(Team team, org.bukkit.entity.Player player) {
        if (!hasBoard()) return;
        Barracks barracks = barracksMap.get(team);
        if (barracks != null) {
            player.teleport(barracks.spawnLocation());
        }
    }

    public void addBarracksChest(Location location, Team team) {
        Location blockLoc = location.getBlock().getLocation();
        barracksChests.add(blockLoc);
        chestTeamOwnership.put(blockLoc, team);
    }

    public void deployToBattlefield(Team team, Coordinate startCoordinate, org.bukkit.entity.Player player) {
        if (!hasBoard()) return;
        
        Location spawnLocation = currentBoard.toCenterLocation(startCoordinate).add(0, 1, 0);
        if (team == Team.WHITE) {
            spawnLocation.setDirection(currentBoard.forward().getDirection());
        } else {
            spawnLocation.setDirection(currentBoard.forward().getDirection().multiply(-1));
        }
        
        player.teleport(spawnLocation);
    }

    public boolean isBarracksChest(Location location) {
        return barracksChests.contains(location.getBlock().getLocation());
    }

    public boolean isTeamChest(Location location, Team team) {
        return chestTeamOwnership.get(location.getBlock().getLocation()) == team;
    }

    public void clearBarracksChests() {
        for (Location loc : barracksChests) {
            org.bukkit.block.Block block = loc.getBlock();
            if (block.getState() instanceof InventoryHolder holder) {
                Inventory inv = holder.getInventory();
                new ArrayList<>(inv.getViewers()).forEach(org.bukkit.entity.HumanEntity::closeInventory);
                inv.clear();
            }
            block.setType(Material.AIR);
        }
        barracksChests.clear();
        chestTeamOwnership.clear();
    }
}
