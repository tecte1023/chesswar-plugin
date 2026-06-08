package dev.tecte.chesswar.board;

import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class BoardManager implements Listener {
    public static final String TURN_ORDER_KEY = "turn_order";
    public static final String READY_BUTTON_KEY = "ready_button";

    private static final double PLAYER_DEPLOY_Y_OFFSET = 1.0;
    private static final int READY_BTN_SLOT_54 = 49;
    private static final int READY_BTN_SLOT_27 = 22;

    private final NamespacedKey readyKey;
    private final NamespacedKey orderKey;
    private final BoardState boardState;

    public boolean hasBoard() {
        return boardState.currentBoard() != null;
    }

    public ChessBoard currentBoard() {
        return boardState.currentBoard();
    }

    public void updateBoard(final ChessBoard board) {
        if (boardState.currentBoard() != null) {
            log.info("새로운 체스판이 설정되어 기존 체스판의 데이터를 덮어씁니다.");
            clearBarracksChests();
        }

        boardState.currentBoard(board);
        boardState.barracksMap().put(Team.WHITE, new Barracks(Team.WHITE, board));
        boardState.barracksMap().put(Team.BLACK, new Barracks(Team.BLACK, board));
    }

    public ChessBoard getBarracksBoard(final Team team) {
        final Barracks barracks = boardState.barracksMap().get(team);
        return (barracks != null) ? barracks.board() : null;
    }

    public void setupBarracks(final PieceManager pieceManager) {
        if (!hasBoard()) return;

        final PieceType[] backRow = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (final Team team : Team.values()) {
            final Barracks barracks = boardState.barracksMap().get(team);
            final int y = (team == Team.WHITE) ? 0 : 7;

            for (int x = 0; x < 8; x++) {
                pieceManager.spawnPiece(
                        backRow[x],
                        team,
                        Coordinate.of(x, y),
                        barracks.board().toCenterLocation(Coordinate.of(x, y)),
                        barracks.board().forward().getDirection().multiply(team == Team.WHITE ? 1 : -1),
                        true
                );
            }
        }
    }

    public void setupTurnOrderChests(final int whiteCount, final int blackCount) {
        if (!hasBoard()) return;

        setupReadyChest(boardState.barracksMap().get(Team.WHITE), whiteCount);
        setupReadyChest(boardState.barracksMap().get(Team.BLACK), blackCount);
    }

    private void setupReadyChest(final Barracks barracks, final int participantCount) {
        final Location b1 = barracks.chestLocation1();
        final Location b2 = barracks.chestLocation2();

        b1.getBlock().setType(Material.CHEST, false);
        b2.getBlock().setType(Material.CHEST, false);

        final BlockState state1 = b1.getBlock().getState();
        final BlockState state2 = b2.getBlock().getState();

        final Chest data1 = (Chest) state1.getBlockData();
        final Chest data2 = (Chest) state2.getBlockData();

        data1.setFacing(barracks.chestFacing());
        data2.setFacing(barracks.chestFacing());

        // 팀 방향에 따라 좌/우 설정 (흑팀은 방향이 반대이므로 반전 필요)
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

        addBarracksChest(b1, barracks.team());
        addBarracksChest(b2, barracks.team());

        final boolean b1IsTop = b1.getBlockX() < b2.getBlockX() || (b1.getBlockX() == b2.getBlockX() && b1.getBlockZ() < b2.getBlockZ());
        final Inventory chestInv = ((InventoryHolder) (b1IsTop ? b1 : b2).getBlock().getState()).getInventory();

        chestInv.clear();

        if (participantCount > 0) {
            for (int i = 1; i <= participantCount; i++) {
                final ItemStack item = new ItemStack(Material.PAPER);
                final ItemMeta meta = item.getItemMeta();

                meta.displayName(Component.text(i + "번 순서", NamedTextColor.GOLD, TextDecoration.BOLD));
                meta.getPersistentDataContainer().set(orderKey, PersistentDataType.INTEGER, i);
                item.setItemMeta(meta);
                chestInv.addItem(item);
            }
        }

        final ItemStack readyBtn = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        final ItemMeta readyMeta = readyBtn.getItemMeta();

        readyMeta.displayName(Component.text("[ 준비 완료 ]", NamedTextColor.GREEN, TextDecoration.BOLD));
        readyMeta.getPersistentDataContainer().set(readyKey, PersistentDataType.BYTE, (byte) 1);
        readyBtn.setItemMeta(readyMeta);

        final int readySlot = (chestInv.getSize() == 54) ? READY_BTN_SLOT_54 : READY_BTN_SLOT_27;
        chestInv.setItem(readySlot, readyBtn);
    }

    public void disableReadyButton(final Team team) {
        final Barracks barracks = boardState.barracksMap().get(team);
        if (barracks == null) return;

        final Location b1 = barracks.chestLocation1();
        final Location b2 = barracks.chestLocation2();

        final boolean b1IsTop = b1.getBlockX() < b2.getBlockX() || (b1.getBlockX() == b2.getBlockX() && b1.getBlockZ() < b2.getBlockZ());
        final org.bukkit.block.Block chestBlock = (b1IsTop ? b1 : b2).getBlock();

        if (!(chestBlock.getState() instanceof InventoryHolder holder)) return;

        final Inventory chestInv = holder.getInventory();
        final int readySlot = (chestInv.getSize() == 54) ? READY_BTN_SLOT_54 : READY_BTN_SLOT_27;

        final ItemStack disabledBtn = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
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
        if (!hasBoard()) return;
        final Barracks barracks = boardState.barracksMap().get(team);
        if (barracks != null) {
            player.teleport(barracks.spawnLocation());
        }
    }

    public void addBarracksChest(final Location location, final Team team) {
        final Location blockLoc = location.getBlock().getLocation();
        boardState.barracksChests().add(blockLoc);
        boardState.chestTeamOwnership().put(blockLoc, team);
    }

    public void deployToBattlefield(final Team team, final Coordinate startCoordinate, final Player player) {
        if (!hasBoard()) return;

        final Location spawnLocation = boardState.currentBoard().toCenterLocation(startCoordinate).add(0, PLAYER_DEPLOY_Y_OFFSET, 0);
        if (team == Team.WHITE) {
            spawnLocation.setDirection(boardState.currentBoard().forward().getDirection());
        } else {
            spawnLocation.setDirection(boardState.currentBoard().forward().getDirection().multiply(-1));
        }

        player.teleport(spawnLocation);
    }

    public boolean isBarracksChest(final Location location) {
        return boardState.barracksChests().contains(location.getBlock().getLocation());
    }

    public boolean isTeamChest(final Location location, final Team team) {
        return boardState.chestTeamOwnership().get(location.getBlock().getLocation()) == team;
    }

    public void clearBarracksChests() {
        for (final Location loc : boardState.barracksChests()) {
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
        boardState.barracksChests().clear();
        boardState.chestTeamOwnership().clear();
    }
}
