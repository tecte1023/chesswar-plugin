package dev.tecte.chessWar.board.domain.model;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Orientation(
        Vector forward,
        Vector left,
        Vector right
) {
    @Contract("_ -> new")
    public static @NotNull Orientation from(@NotNull BlockFace facing) {
        Vector forward = facing.getDirection();
        Vector left = BlockFace.UP.getDirection().crossProduct(forward);
        Vector right = left.clone().multiply(-1);

        return new Orientation(forward, left, right);
    }
}
