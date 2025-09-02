package dev.tecte.chessWar.board.domain.model;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

@Getter
public enum Orientation {
    NORTH(BlockFace.NORTH),
    EAST(BlockFace.EAST),
    SOUTH(BlockFace.SOUTH),
    WEST(BlockFace.WEST);

    private final Vector forward;
    private final Vector backward;
    private final Vector left;
    private final Vector right;

    Orientation(@NonNull BlockFace blockFace) {
        forward = blockFace.getDirection();
        backward = forward.clone().multiply(-1);
        left = BlockFace.UP.getDirection().crossProduct(forward);
        right = left.clone().multiply(-1);
    }

    @NonNull
    public static Orientation from(@NonNull BlockFace blockFace) {
        return switch (blockFace) {
            case EAST, EAST_NORTH_EAST, EAST_SOUTH_EAST -> EAST;
            case SOUTH, SOUTH_EAST, SOUTH_WEST -> SOUTH;
            case WEST, WEST_NORTH_WEST, WEST_SOUTH_WEST -> WEST;
            default -> NORTH;
        };
    }
}
