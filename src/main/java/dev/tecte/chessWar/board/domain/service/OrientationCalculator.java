package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.Orientation;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class OrientationCalculator {
    public Orientation createOrientation(float yaw) {
        float normalizedYaw = (yaw + 180) % 360;
        BlockFace facing = switch (Math.round(normalizedYaw / 90) % 4) {
            case 0 -> BlockFace.NORTH;
            case 1 -> BlockFace.WEST;
            case 2 -> BlockFace.SOUTH;
            default -> BlockFace.EAST;
        };
        Vector forward = facing.getDirection();
        Vector up = BlockFace.UP.getDirection();
        Vector left = up.crossProduct(forward);
        Vector right = forward.crossProduct(up);

        return Orientation.builder()
                .up(up)
                .forward(forward)
                .left(left)
                .right(right)
                .build();
    }
}
