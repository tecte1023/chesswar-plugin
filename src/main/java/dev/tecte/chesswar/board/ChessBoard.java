package dev.tecte.chesswar.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

@Getter
@Accessors(fluent = true)
public class ChessBoard {
    private final Location origin;
    private final BlockFace forward;
    private final BlockFace right;
    private final int cellSize;

    public ChessBoard(Location origin, BlockFace forward, int cellSize) {
        this.origin = origin.getBlock().getLocation();
        this.forward = forward;
        this.right = getRightFace(forward);
        this.cellSize = cellSize;
    }

    public Location toLocation(Coordinate coord) {
        Vector offset = forward.getDirection()
                .multiply(coord.y() * cellSize)
                .add(right.getDirection().multiply(coord.x() * cellSize));

        return origin.clone().add(offset);
    }

    public Coordinate toCoordinate(Location location) {
        Vector relative = location.toVector().subtract(origin.toVector());
        int y = (int) Math.round(relative.dot(forward.getDirection()) / cellSize);
        int x = (int) Math.round(relative.dot(right.getDirection()) / cellSize);

        return Coordinate.of(x, y);
    }

    private BlockFace getRightFace(BlockFace forward) {
        return switch (forward) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case EAST -> BlockFace.SOUTH;
            case WEST -> BlockFace.NORTH;
            default -> throw new IllegalArgumentException("지원하지 않는 방향입니다: " + forward);
        };
    }
}
