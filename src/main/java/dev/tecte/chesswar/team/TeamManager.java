package dev.tecte.chesswar.team;

import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.GamePhaseComponent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public final class TeamManager {
    private static final int MAX_ROSTER_SIZE = 8;

    @NotNull
    private final TeamRosterComponent rosterComponent;

    @NotNull
    private final GamePhaseComponent gamePhaseComponent;

    @NotNull
    public JoinResult tryJoinTeam(@NotNull final Player player, @NotNull final TeamSide teamSide) {
        if (gamePhaseComponent.phase() != GamePhase.WAITING) {
            return JoinResult.INVALID_PHASE;
        }

        final UUID playerId = player.getUniqueId();
        final JoinResult checkResult = checkAndLeaveExistingTeam(playerId, teamSide);

        if (checkResult != JoinResult.SUCCESS) {
            return checkResult;
        }

        final UUID[] targetRoster = rosterComponent.teamRosters()[teamSide.ordinal()];

        if (targetRoster.length >= MAX_ROSTER_SIZE) {
            return JoinResult.TEAM_FULL;
        }

        rosterComponent.teamRosters()[teamSide.ordinal()] = withElement(targetRoster, playerId);

        return JoinResult.SUCCESS;
    }

    @NotNull
    private JoinResult checkAndLeaveExistingTeam(@NotNull final UUID playerId, @NotNull final TeamSide targetTeamSide) {
        final UUID[][] rosters = rosterComponent.teamRosters();

        for (int teamIndex = 0; teamIndex < rosters.length; teamIndex++) {
            final UUID[] currentRoster = rosters[teamIndex];

            for (int playerIndex = 0; playerIndex < currentRoster.length; playerIndex++) {
                if (!currentRoster[playerIndex].equals(playerId)) {
                    continue;
                }

                if (teamIndex == targetTeamSide.ordinal()) {
                    return JoinResult.ALREADY_IN_TEAM;
                }

                rosters[teamIndex] = withoutElement(currentRoster, playerIndex);

                return JoinResult.SUCCESS;
            }
        }

        return JoinResult.SUCCESS;
    }

    @NotNull
    private UUID[] withElement(@NotNull final UUID[] elements, @NotNull final UUID element) {
        final var newElements = new UUID[elements.length + 1];

        System.arraycopy(elements, 0, newElements, 0, elements.length);
        newElements[elements.length] = element;

        return newElements;
    }

    @NotNull
    public LeaveResult tryLeaveTeam(@NotNull final Player player, @NotNull final TeamSide teamSide) {
        if (gamePhaseComponent.phase() != GamePhase.WAITING) {
            return LeaveResult.INVALID_PHASE;
        }

        final UUID[] roster = rosterComponent.teamRosters()[teamSide.ordinal()];

        for (int i = 0; i < roster.length; i++) {
            if (!roster[i].equals(player.getUniqueId())) {
                continue;
            }

            rosterComponent.teamRosters()[teamSide.ordinal()] = withoutElement(roster, i);

            return LeaveResult.SUCCESS;
        }

        return LeaveResult.NOT_IN_TEAM;
    }

    @NotNull
    private UUID[] withoutElement(@NotNull final UUID[] elements, final int index) {
        final var newElements = new UUID[elements.length - 1];

        System.arraycopy(elements, 0, newElements, 0, index);
        System.arraycopy(elements, index + 1, newElements, index, elements.length - index - 1);

        return newElements;
    }

    @NotNull
    public UUID[][] teamRosters() {
        return rosterComponent.teamRosters();
    }
}
