package dev.tecte.chesswar.team;

import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.GamePhaseComponent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public final class TeamManager {
    @NotNull
    private final TeamRosterComponent rosterComponent;

    @NotNull
    private final GamePhaseComponent gamePhaseComponent;

    @NotNull
    private final TeamPresenter presenter;

    public void tryJoinTeam(@NotNull final Player player, @NotNull final TeamSide teamSide) {
        if (gamePhaseComponent.phase() != GamePhase.WAITING) {
            return;
        }

        final UUID playerId = player.getUniqueId();
        final UUID[] targetRoster = rosterComponent.teamRosters()[teamSide.ordinal()];

        for (final UUID uuid : targetRoster) {
            if (uuid.equals(playerId)) {
                return;
            }
        }

        for (final TeamSide t : TeamSide.values()) {
            leaveTeamInternal(playerId, t);
        }

        rosterComponent.teamRosters()[teamSide.ordinal()] = withElement(targetRoster, playerId);
        presenter.sendJoinMessage(player, teamSide);
    }

    @NotNull
    private UUID[] withElement(@NotNull final UUID[] elements, @NotNull final UUID element) {
        final UUID[] newElements = new UUID[elements.length + 1];

        System.arraycopy(elements, 0, newElements, 0, elements.length);
        newElements[elements.length] = element;

        return newElements;
    }

    public void tryLeaveTeam(@NotNull final Player player, @NotNull final TeamSide teamSide) {
        if (gamePhaseComponent.phase() != GamePhase.WAITING) {
            return;
        }

        if (!leaveTeamInternal(player.getUniqueId(), teamSide)) {
            return;
        }

        presenter.sendLeaveMessage(player);
    }

    private boolean leaveTeamInternal(@NotNull final UUID playerId, @NotNull final TeamSide teamSide) {
        final UUID[] roster = rosterComponent.teamRosters()[teamSide.ordinal()];

        for (int i = 0; i < roster.length; i++) {
            if (roster[i].equals(playerId)) {
                rosterComponent.teamRosters()[teamSide.ordinal()] = withoutElement(roster, i);
                return true;
            }
        }

        return false;
    }

    @NotNull
    private UUID[] withoutElement(@NotNull final UUID[] elements, final int index) {
        final UUID[] newElements = new UUID[elements.length - 1];

        System.arraycopy(elements, 0, newElements, 0, index);
        System.arraycopy(elements, index + 1, newElements, index, elements.length - index - 1);

        return newElements;
    }
}
