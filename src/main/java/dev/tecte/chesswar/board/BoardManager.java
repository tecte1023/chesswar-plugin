package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class BoardManager {
    @NotNull
    private final BoardComponent boardComponent;

    @NotNull
    private final BoardEnvironmentPresenter boardEnvPresenter;

    public void updateBoard(@NotNull final Board newBoard) {
        if (boardComponent.hasBoard()) {
            boardEnvPresenter.clearBarracksChests();
        }

        boardComponent.board(newBoard);
    }

    public boolean isBarracksChest(@NotNull final Location location) {
        for (final Team team : Team.values()) {
            if (isTeamChest(location, team)) {
                return true;
            }
        }

        return false;
    }

    public boolean isTeamChest(@NotNull final Location location, @NotNull final Team team) {
        if (boardComponent.isEmpty()) {
            return false;
        }

        final Barracks barracks = boardComponent.board().getBarracks(team);

        if (barracks == null) {
            return false;
        }

        return location.equals(barracks.leftChestLocation()) || location.equals(barracks.rightChestLocation());
    }
}
