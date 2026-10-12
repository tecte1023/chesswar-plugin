package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.core.View;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PieceView implements View {
    private final BoardContext board;
    private final Map<UUID, UUID> visualEntities = new HashMap<>();
    private final Vector tempWorldPos = new Vector();

    public static PieceView create(final SimulationEventBus eventBus, final BoardContext board) {
        PieceView view = new PieceView(board);
        eventBus.subscribe(PieceSpawnEvent.class, view::onPieceSpawn);
        return view;
    }

    private void onPieceSpawn(final PieceSpawnEvent event) {
        GridComponent gridComponent = event.piece().getComponent(GridComponent.class);
        if (gridComponent == null) {
            return;
        }

        World world = Bukkit.getWorld(board.worldId());
        if (world == null) {
            return;
        }

        board.applyWorldPos(tempWorldPos, gridComponent.gridPos());
        Location loc = tempWorldPos.toLocation(world);
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);

        armorStand.setGravity(false);
        armorStand.setCustomName("Piece " + event.piece().id().toString().substring(0, 4));
        armorStand.setCustomNameVisible(true);

        visualEntities.put(event.piece().id(), armorStand.getUniqueId());
    }
}
