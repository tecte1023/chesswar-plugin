package dev.tecte.chesswar.board;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Grid {
    public static final int CELL_SIZE = 3;
    public static final int PHYSICAL_LENGTH = Coordinate.BOARD_SIZE * CELL_SIZE;
    public static final double CELL_OFFSET = CELL_SIZE / 2.0;

    @NotNull
    private final Location anchor;

    @NotNull
    @Getter
    private final BlockFace forward;

    @NotNull
    @Getter
    private final BlockFace right;

    private final double[] worldXTable;
    private final double[] worldZTable;

    @NotNull
    public static Grid create(@NotNull final Location anchor) {
        final Location location = anchor.toBlockLocation();
        final BlockFace forward = yawToFace(anchor.getYaw());
        final BlockFace right = toRightFace(forward);

        final double[] worldXTable = new double[Coordinate.SQUARE_COUNT];
        final double[] worldZTable = new double[Coordinate.SQUARE_COUNT];

        final int fModX = forward.getModX();
        final int fModZ = forward.getModZ();
        final int rModX = right.getModX();
        final int rModZ = right.getModZ();

        final double startX = location.x() + CELL_OFFSET;
        final double startZ = location.z() + CELL_OFFSET;

        for (int gridY = 0; gridY < Coordinate.BOARD_SIZE; gridY++) {
            for (int gridX = 0; gridX < Coordinate.BOARD_SIZE; gridX++) {
                final int index = Coordinate.flatten(gridX, gridY);

                worldXTable[index] = startX + (gridX * rModX * CELL_SIZE) + (gridY * fModX * CELL_SIZE);
                worldZTable[index] = startZ + (gridX * rModZ * CELL_SIZE) + (gridY * fModZ * CELL_SIZE);
            }
        }

        return new Grid(location, forward, right, worldXTable, worldZTable);
    }

    @NotNull
    public Location getCenterAt(@NotNull final Coordinate coordinate) {
        return applyCenterTo(anchor.clone(), coordinate);
    }

    @NotNull
    public Location applyCenterTo(@NotNull final Location target, @NotNull final Coordinate coordinate) {
        final int index = coordinate.flatIndex();

        target.setWorld(anchor.getWorld());
        target.set(worldXTable[index], anchor.y(), worldZTable[index]);

        return target;
    }

    @NotNull
    public Location anchor() {
        return anchor.clone();
    }

    public double getWorldX(@NotNull final Coordinate coordinate) {
        return worldXTable[coordinate.flatIndex()];
    }

    public double getWorldZ(@NotNull final Coordinate coordinate) {
        return worldZTable[coordinate.flatIndex()];
    }

    @NotNull
    private static BlockFace yawToFace(final float yaw) {
        final int directionIndex = Math.floorMod(Math.round(yaw / 90f), 4);

        return switch (directionIndex) {
            case 0 -> BlockFace.SOUTH;
            case 1 -> BlockFace.WEST;
            case 2 -> BlockFace.NORTH;
            case 3 -> BlockFace.EAST;
            default -> throw new IllegalStateException("계산 범위를 벗어난 각도입니다: " + directionIndex);
        };
    }

    @NotNull
    private static BlockFace toRightFace(@NotNull final BlockFace forward) {
        return switch (forward) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case EAST -> BlockFace.SOUTH;
            case WEST -> BlockFace.NORTH;
            default -> throw new IllegalArgumentException("지원하지 않는 방향: " + forward);
        };
    }
}
