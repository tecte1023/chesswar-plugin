package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Barracks {
    private static final int CENTER_LEFT_X = 3;
    private static final int CENTER_RIGHT_X = 4;

    private static final int WHITE_SPAWN_Y = 6;
    private static final int BLACK_SPAWN_Y = 1;

    private static final int WHITE_CHEST_Y = 4;
    private static final int BLACK_CHEST_Y = 3;

    private static final int CHEST_BLOCK_OFFSET = Grid.CELL_SIZE / 2;

    @NotNull
    @Getter
    private final Team team;

    @NotNull
    private final Grid grid;

    @NotNull
    public static Barracks create(@NotNull final Team team, @NotNull final Location anchor) {
        return new Barracks(team, Grid.create(anchor));
    }

    @NotNull
    public Location spawnLocation() {
        final BlockFace right = grid.right();

        return grid.getCenterAt(CENTER_LEFT_X, isWhite() ? WHITE_SPAWN_Y : BLACK_SPAWN_Y)
                .add(right.getModX() * Grid.CELL_OFFSET, 0, right.getModZ() * Grid.CELL_OFFSET);
    }

    @NotNull
    public Location leftChestLocation() {
        return getOffsetLocation(
                isWhite() ? CENTER_LEFT_X : CENTER_RIGHT_X,
                isWhite() ? WHITE_CHEST_Y : BLACK_CHEST_Y,
                CHEST_BLOCK_OFFSET * team.direction()
        );
    }

    @NotNull
    public Location rightChestLocation() {
        return getOffsetLocation(
                isWhite() ? CENTER_RIGHT_X : CENTER_LEFT_X,
                isWhite() ? WHITE_CHEST_Y : BLACK_CHEST_Y,
                -CHEST_BLOCK_OFFSET * team.direction()
        );
    }

    @NotNull
    public BlockFace chestFacing() {
        return isWhite() ? grid.forward().getOppositeFace() : grid.forward();
    }

    @NotNull
    private Location getOffsetLocation(final int gridX, final int gridY, final int rightOffset) {
        final BlockFace right = grid.right();

        return grid.getCenterAt(gridX, gridY)
                .add(right.getModX() * rightOffset, 0, right.getModZ() * rightOffset);
    }

    private boolean isWhite() {
        return team == Team.WHITE;
    }
}
