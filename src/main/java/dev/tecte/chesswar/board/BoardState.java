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

    public float oppositeYaw() {
        return Location.normalizeYaw(yaw + 180.0f);
    }

    public Vector toWorldPos(final GridPoint gridPos) {
        Vector rightOffset = right.clone().multiply(gridPos.x() * TILE_SIZE);
        Vector forwardOffset = forward.clone().multiply(gridPos.y() * TILE_SIZE);
        return worldOrigin.clone().add(rightOffset).add(forwardOffset);
    }

    public GridPoint toGridPos(final Vector worldPos) {
        Vector offset = worldPos.clone().subtract(worldOrigin);
        double rightDist = offset.dot(right);
        double forwardDist = offset.dot(forward);
        double centerOffset = TILE_SIZE / 2.0;
        int gridX = (int) Math.floor((rightDist + centerOffset) / TILE_SIZE);
        int gridY = (int) Math.floor((forwardDist + centerOffset) / TILE_SIZE);

        return new GridPoint(gridX, gridY);
    }

    public boolean isInBounds(final GridPoint gridPos) {
        return gridPos.x() >= 0 && gridPos.x() < BOARD_SIZE && gridPos.y() >= 0 && gridPos.y() < BOARD_SIZE;
    }
}
