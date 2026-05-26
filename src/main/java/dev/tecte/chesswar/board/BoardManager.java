package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Getter
@Accessors(fluent = true)
public class BoardManager {
    private ChessBoard currentBoard;
    private final Set<Location> barracksChests = new HashSet<>();
    private final Map<Location, Team> chestTeamOwnership = new HashMap<>();

    public boolean hasBoard() {
        return currentBoard != null;
    }

    public void currentBoard(ChessBoard board) {
        if (currentBoard != null) {
            log.info("새로운 체스판이 설정되어 기존 체스판의 데이터를 덮어씁니다.");
        }

        currentBoard = board;
    }

    public void addBarracksChest(Location location, Team team) {
        Location blockLoc = location.getBlock().getLocation();
        barracksChests.add(blockLoc);
        chestTeamOwnership.put(blockLoc, team);
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
