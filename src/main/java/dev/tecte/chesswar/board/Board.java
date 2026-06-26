package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.AccessLevel;
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
    private final Grid grid;

    @NotNull
    private final Map<Team, Barracks> teamBarracks;

    @NotNull
    public static Board create(@NotNull final Location mainAnchor) {
        final Grid mainGrid = Grid.create(mainAnchor);
        final BlockFace forward = mainGrid.forward();
        final Map<Team, Barracks> teamBarracks = new EnumMap<>(Team.class);

        for (final Team team : Team.values()) {
            teamBarracks.put(team, Barracks.create(team, calculateBarracksAnchor(mainAnchor, forward, team)));
        }

        return new Board(mainGrid, Collections.unmodifiableMap(teamBarracks));
    }

    @NotNull
    private static Location calculateBarracksAnchor(
            @NotNull final Location mainAnchor,
            @NotNull final BlockFace forward,
            @NotNull final Team team
    ) {
        final Location anchor = mainAnchor.clone();
        final int offset = BARRACKS_OFFSET * team.direction();

        return anchor.add(forward.getModX() * offset, 0, forward.getModZ() * offset);
    }

    @NotNull
    public Barracks getBarracks(@NotNull final Team team) {
        return teamBarracks.get(team);
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
