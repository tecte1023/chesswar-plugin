package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true)
public final class ChessBoard {
    public static final int BOARD_SIZE = 8;

    private static final double VISUAL_Y_OFFSET = 0.1;
    private static final double BLOCK_CENTER_OFFSET = 0.5;

    @NotNull
    private final Location origin;

    @NotNull
    private final World world;

    @NotNull
    @Getter
    private final BlockFace forward;

    @NotNull
    @Getter
    private final BlockFace right;

    @NotNull
    private final Vector forwardVector;

    @NotNull
    private final Vector rightVector;

    @NotNull
    private final Vector whiteDirection;

    @NotNull
    private final Vector blackDirection;

    @Getter
    private final int cellSize;

    private final double halfCell;

    private ChessBoard(@NotNull final Location origin, @NotNull final BlockFace forward, final int cellSize) {
        this.origin = origin.toBlockLocation().add(BLOCK_CENTER_OFFSET, 0.0, BLOCK_CENTER_OFFSET);
        world = origin.getWorld();
        this.forward = forward;
        right = getRightFace(forward);
        forwardVector = forward.getDirection();
        rightVector = right.getDirection();
        whiteDirection = new Vector(forwardVector.getX(), 0, forwardVector.getZ());
        blackDirection = new Vector(-forwardVector.getX(), 0, -forwardVector.getZ());
        this.cellSize = cellSize;
        halfCell = cellSize / 2.0;
    }

    @NotNull
    public static ChessBoard of(@NotNull final Location origin, @NotNull final BlockFace forward, final int cellSize) {
        return new ChessBoard(origin, forward, cellSize);
    }

    @NotNull
    public Location origin() {
        return origin.clone();
    }

    @NotNull
    public Vector calculateDirection(@NotNull final Team team) {
        return switch (team) {
            case WHITE -> whiteDirection.clone();
            case BLACK -> blackDirection.clone();
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
        final double gridPosX = (coordinate.x() * cellSize) + halfCell;
        final double gridPosY = (coordinate.y() * cellSize) + halfCell;
        final double offsetX = (rightVector.getX() * gridPosX) + (forwardVector.getX() * gridPosY);
        final double offsetZ = (rightVector.getZ() * gridPosX) + (forwardVector.getZ() * gridPosY);

        target.setX(origin.getX() + offsetX);
        target.setY(origin.getY() + VISUAL_Y_OFFSET);
        target.setZ(origin.getZ() + offsetZ);
        target.setWorld(origin.getWorld());

        return target;
    }

    @Nullable
    public Coordinate toCoordinate(@NotNull final Location location) {
        final World locationWorld = location.getWorld();

        if (locationWorld == null || !locationWorld.equals(world)) {
            return null;
        }

        final int blockOffsetX = location.getBlockX() - origin.getBlockX();
        final int blockOffsetZ = location.getBlockZ() - origin.getBlockZ();
        final int forwardDot = (blockOffsetX * forward.getModX()) + (blockOffsetZ * forward.getModZ());
        final int rightDot = (blockOffsetX * right.getModX()) + (blockOffsetZ * right.getModZ());
        final int y = Math.floorDiv(forwardDot, cellSize);
        final int x = Math.floorDiv(rightDot, cellSize);

        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
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
