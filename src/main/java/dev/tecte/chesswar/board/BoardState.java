package dev.tecte.chesswar.board;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.UUID;

public record BoardState(UUID worldId, Vector worldOrigin, Vector forward, Vector right, float yaw) {
    public static final int BOARD_SIZE = 8;
    public static final int TILE_SIZE = 3;
    private static final BlockFace[] FACES = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};

    public static BoardState create(final Location loc) {
        int directionIndex = Math.floorMod(Math.round(loc.getYaw() / 90f), 4);
        BlockFace forwardFace = FACES[directionIndex];
        BlockFace rightFace = FACES[(directionIndex + 1) % 4];

        var worldOrigin = new Vector(loc.getBlockX() + 0.5, loc.getY(), loc.getBlockZ() + 0.5);
        var forward = new Vector(forwardFace.getModX(), 0, forwardFace.getModZ());
        var right = new Vector(rightFace.getModX(), 0, rightFace.getModZ());
        float yaw = directionIndex * 90f;

        return new BoardState(loc.getWorld().getUID(), worldOrigin, forward, right, yaw);
    }

    public void applyWorldPos(final Vector targetWorldPos, final GridPoint gridPos) {
        double worldX = worldOrigin.getX() + ((right.getX() * gridPos.x() + forward.getX() * gridPos.y()) * TILE_SIZE);
        double worldY = worldOrigin.getY();
        double worldZ = worldOrigin.getZ() + ((right.getZ() * gridPos.x() + forward.getZ() * gridPos.y()) * TILE_SIZE);

        targetWorldPos.setX(worldX).setY(worldY).setZ(worldZ);
    }

    public Vector toWorldPos(final GridPoint gridPos) {
        var targetWorldPos = new Vector();
        applyWorldPos(targetWorldPos, gridPos);
        return targetWorldPos;
    }

    public boolean isInBounds(final int gridX, final int gridY) {
        return gridX >= 0 && gridX < BOARD_SIZE && gridY >= 0 && gridY < BOARD_SIZE;
    }
}
