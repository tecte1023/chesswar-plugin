package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.Orientation;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class OrientationCalculator {
    public Orientation createOrientation(BlockFace facing) {
        Vector forward = facing.getDirection();
        Vector left = BlockFace.UP.getDirection().crossProduct(forward);
        Vector right = facing.getDirection().crossProduct(BlockFace.UP.getDirection());

        return Orientation.builder()
                .forward(forward)
                .left(left)
                .right(right)
                .build();
    }
}
