package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Barracks {
    private static final Coordinate WHITE_SPAWN = Coordinate.of(3, 6);
    private static final Coordinate WHITE_LEFT_CHEST = Coordinate.of(3, 4);
    private static final Coordinate WHITE_RIGHT_CHEST = Coordinate.of(4, 4);

    private static final Coordinate BLACK_SPAWN = Coordinate.of(3, 1);
    private static final Coordinate BLACK_LEFT_CHEST = Coordinate.of(4, 3);
    private static final Coordinate BLACK_RIGHT_CHEST = Coordinate.of(3, 3);

    private static final int CHEST_BLOCK_OFFSET = Grid.CELL_SIZE / 2;

    @NotNull
    private final Team team;

    @NotNull
    private final Grid grid;

    @NotNull
    private final Location spawnLocation;

    @NotNull
    private final Location leftChestLocation;

    @NotNull
    private final Location rightChestLocation;

    @NotNull
    private final BlockFace chestFacing;

    @NotNull
    public static Barracks create(@NotNull final Team team, @NotNull final Location anchor) {
        final Grid grid = Grid.create(anchor);
        final boolean isWhite = team == Team.WHITE;

        final int chestOffset = CHEST_BLOCK_OFFSET * team.direction();
        final BlockFace right = grid.right();

        final Location spawnLocation = grid.getCenterAt(isWhite ? WHITE_SPAWN : BLACK_SPAWN)
                .add(right.getModX() * Grid.CELL_OFFSET, 0, right.getModZ() * Grid.CELL_OFFSET);
        final Location leftChestLocation = grid.getCenterAt(isWhite ? WHITE_LEFT_CHEST : BLACK_LEFT_CHEST)
                .add(right.getModX() * chestOffset, 0, right.getModZ() * chestOffset);
        final Location rightChestLocation = grid.getCenterAt(isWhite ? WHITE_RIGHT_CHEST : BLACK_RIGHT_CHEST)
                .add(right.getModX() * -chestOffset, 0, right.getModZ() * -chestOffset);
        final BlockFace chestFacing = isWhite ? grid.forward().getOppositeFace() : grid.forward();

        return new Barracks(team, grid, spawnLocation, leftChestLocation, rightChestLocation, chestFacing);
    }

    @NotNull
    public Location spawnLocation() {
        return spawnLocation.clone();
    }

    @NotNull
    public Location leftChestLocation() {
        return leftChestLocation.clone();
    }

    @NotNull
    public Location rightChestLocation() {
        return rightChestLocation.clone();
    }
}
