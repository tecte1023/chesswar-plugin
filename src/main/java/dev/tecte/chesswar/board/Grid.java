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
    private static final double CENTER_OFFSET = (CELL_SIZE - 1) / 2.0;
    private static final BlockFace[] FACES = {
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST
    };

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
        final Location location = anchor.toCenterLocation();
        final int directionIndex = Math.floorMod(Math.round(anchor.getYaw() / 90f), 4);
        final float snappedYaw = directionIndex == 3 ? -90f : directionIndex * 90f;
        final BlockFace forward = FACES[directionIndex];
        final BlockFace right = FACES[(directionIndex + 1) % 4];

        location.setY(anchor.getBlockY());
        location.setYaw(snappedYaw);
        location.setPitch(0f);

        final double[] worldXTable = new double[Coordinate.SQUARE_COUNT];
        final double[] worldZTable = new double[Coordinate.SQUARE_COUNT];

        final int fModX = forward.getModX();
        final int fModZ = forward.getModZ();
        final int rModX = right.getModX();
        final int rModZ = right.getModZ();

        final double startX = location.x() + (rModX * CENTER_OFFSET) + (fModX * CENTER_OFFSET);
        final double startZ = location.z() + (rModZ * CENTER_OFFSET) + (fModZ * CENTER_OFFSET);

        final int forwardStepX = fModX * CELL_SIZE;
        final int forwardStepZ = fModZ * CELL_SIZE;
        final int rightStepX = rModX * CELL_SIZE;
        final int rightStepZ = rModZ * CELL_SIZE;

        for (int gridY = 0; gridY < Coordinate.BOARD_SIZE; gridY++) {
            for (int gridX = 0; gridX < Coordinate.BOARD_SIZE; gridX++) {
                final int index = Coordinate.flatten(gridX, gridY);

                worldXTable[index] = startX + (gridX * rightStepX) + (gridY * forwardStepX);
                worldZTable[index] = startZ + (gridX * rightStepZ) + (gridY * forwardStepZ);
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
}
