package dev.tecte.chesswar.board;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.UUID;

public record BoardState(UUID worldId, Vector worldOrigin, Vector forward, Vector right) {
    private static final BlockFace[] FACES = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};

    public static BoardState create(final UUID worldId, final Vector playerWorldPos, final float playerYaw) {
        int directionIndex = Math.floorMod(Math.round(playerYaw / 90f), 4);
        BlockFace forwardFace = FACES[directionIndex];
        BlockFace rightFace = FACES[(directionIndex + 1) % 4];

        return new BoardState(
                worldId,
                playerWorldPos.clone(),
                new Vector(forwardFace.getModX(), 0, forwardFace.getModZ()),
                new Vector(rightFace.getModX(), 0, rightFace.getModZ())
        );
    }

    public void applyWorldPos(final Vector targetWorldPos, final GridPoint gridPos) {
        double worldX = worldOrigin.getX() + (right.getX() * gridPos.x()) + (forward.getX() * gridPos.y());
        double worldY = worldOrigin.getY();
        double worldZ = worldOrigin.getZ() + (right.getZ() * gridPos.x()) + (forward.getZ() * gridPos.y());
        
        targetWorldPos.setX(worldX).setY(worldY).setZ(worldZ);
    }
}
