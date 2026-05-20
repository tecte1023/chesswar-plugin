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

    public Location toLocation(Coordinate coordinate) {
        Vector offset = forward.getDirection()
                .multiply(coordinate.y() * cellSize)
                .add(right.getDirection().multiply(coordinate.x() * cellSize));

        return origin.clone().add(offset);
    }

    public Location toCenterLocation(Coordinate coordinate) {
        double centerOffset = (cellSize - 1) / 2.0;

        return toLocation(coordinate).add(
                forward.getDirection().multiply(centerOffset)
                        .add(right.getDirection().multiply(centerOffset))
        ).add(0.5, 0, 0.5);
    }

    public Coordinate toCoordinate(Location location) {
        Vector relative = location.toVector().subtract(origin.toVector());
        int y = (int) Math.floor(relative.dot(forward.getDirection()) / cellSize);
        int x = (int) Math.floor(relative.dot(right.getDirection()) / cellSize);

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
