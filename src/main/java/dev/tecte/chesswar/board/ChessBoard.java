package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Accessors(fluent = true)
public class ChessBoard {
    private static final double VISUAL_Y_OFFSET = 0.1;
    private static final double BLOCK_CENTER_OFFSET = 0.5;

    private final Location origin;
    private final BlockFace forward;
    private final BlockFace right;
    private final Vector forwardVector;
    private final Vector rightVector;
    private final Vector whiteDirection;
    private final Vector blackDirection;
    private final int cellSize;

    public ChessBoard(@NotNull final Location origin, @NotNull final BlockFace forward, final int cellSize) {
        this.origin = origin.toBlockLocation().add(BLOCK_CENTER_OFFSET, 0.0, BLOCK_CENTER_OFFSET);
        this.forward = forward;
        right = getRightFace(forward);
        forwardVector = forward.getDirection();
        rightVector = right.getDirection();
        whiteDirection = new Vector(forwardVector.getX(), 0, forwardVector.getZ());
        blackDirection = new Vector(-forwardVector.getX(), 0, -forwardVector.getZ());
        this.cellSize = cellSize;
    }

    @NotNull
    public Vector calculateDirection(@NotNull final Team team) {
        return switch (team) {
            case WHITE -> whiteDirection;
            case BLACK -> blackDirection;
        };
    }

    @NotNull
    public Location newLocationBuffer() {
        return origin.clone();
    }

    @NotNull
    public Location toCenterLocation(@NotNull final Coordinate coordinate) {
        return updateToCenterLocation(coordinate, newLocationBuffer());
    }

    @NotNull
    public Location updateToCenterLocation(@NotNull final Coordinate coordinate, @NotNull final Location target) {
        final int halfCell = cellSize / 2;
        final double gridPosX = (coordinate.x() * cellSize) + halfCell;
        final double gridPosY = (coordinate.y() * cellSize) + halfCell;
        final double offsetX = (rightVector.getX() * gridPosX) + (forwardVector.getX() * gridPosY);
        final double offsetZ = (rightVector.getZ() * gridPosX) + (forwardVector.getZ() * gridPosY);

        target.setX(origin.getX() + offsetX);
        target.setY(origin.getY() + VISUAL_Y_OFFSET);
        target.setZ(origin.getZ() + offsetZ);

        return target;
    }

    @Nullable
    public Coordinate toCoordinate(@NotNull final Location location) {
        final int blockOffsetX = location.getBlockX() - origin.getBlockX();
        final int blockOffsetZ = location.getBlockZ() - origin.getBlockZ();
        final int forwardDot = (blockOffsetX * forward.getModX()) + (blockOffsetZ * forward.getModZ());
        final int rightDot = (blockOffsetX * right.getModX()) + (blockOffsetZ * right.getModZ());
        final int y = Math.floorDiv(forwardDot, cellSize);
        final int x = Math.floorDiv(rightDot, cellSize);

        if (x < 0 || x >= ChessFormation.BOARD_SIZE || y < 0 || y >= ChessFormation.BOARD_SIZE) {
            return null;
        }

        return Coordinate.of(x, y);
    }

    @NotNull
    private BlockFace getRightFace(@NotNull final BlockFace forward) {
        return switch (forward) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case EAST -> BlockFace.SOUTH;
            case WEST -> BlockFace.NORTH;
            default -> throw new IllegalArgumentException("지원하지 않는 방향입니다: " + forward);
        };
    }
}
