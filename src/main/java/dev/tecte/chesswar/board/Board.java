package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.TeamSide;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Board {
    private static final int BARRACKS_GAP = 5;
    private static final int BARRACKS_OFFSET = Grid.PHYSICAL_LENGTH + BARRACKS_GAP;

    @NotNull
    @Getter
    private final Grid grid;

    @NotNull
    private final Map<TeamSide, Barracks> teamBarracks;

    @NotNull
    public static Board create(@NotNull final Location mainAnchor) {
        final Grid mainGrid = Grid.create(mainAnchor);
        final BlockFace forward = mainGrid.forward();
        final Map<TeamSide, Barracks> teamBarracks = new EnumMap<>(TeamSide.class);

        for (final TeamSide teamSide : TeamSide.values()) {
            teamBarracks.put(teamSide, Barracks.create(teamSide, calculateBarracksAnchor(mainAnchor, forward, teamSide)));
        }

        return new Board(mainGrid, Collections.unmodifiableMap(teamBarracks));
    }

    @NotNull
    private static Location calculateBarracksAnchor(
            @NotNull final Location mainAnchor,
            @NotNull final BlockFace forward,
            @NotNull final TeamSide teamSide
    ) {
        final Location anchor = mainAnchor.clone();
        final int backwardDirection = -teamSide.direction();
        final int offset = BARRACKS_OFFSET * backwardDirection;

        return anchor.add(forward.getModX() * offset, 0, forward.getModZ() * offset);
    }

    @NotNull
    public Barracks getBarracks(@NotNull final TeamSide teamSide) {
        return teamBarracks.get(teamSide);
    }

    @NotNull
    public Location getCenterAt(@NotNull final Coordinate coordinate) {
        return grid.getCenterAt(coordinate);
    }

    @NotNull
    public Location applyCenterTo(@NotNull final Location target, @NotNull final Coordinate coordinate) {
        return grid.applyCenterTo(target, coordinate);
    }
}
