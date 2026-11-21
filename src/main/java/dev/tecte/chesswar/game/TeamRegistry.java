package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.Team;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class TeamRegistry {
    public static final int MAX_TEAM_SIZE = 8;

    private final Set<UUID> whiteMembers = new LinkedHashSet<>();
    private final Set<UUID> blackMembers = new LinkedHashSet<>();

    public TeamJoinResult join(final UUID playerId, final Team team) {
        Set<UUID> targetMembers = members(team);
        if (targetMembers.contains(playerId)) {
            return TeamJoinResult.ALREADY_IN_TEAM;
        }

        if (targetMembers.size() >= MAX_TEAM_SIZE) {
            return TeamJoinResult.TEAM_FULL;
        }

        oppositeMembers(team).remove(playerId);
        targetMembers.add(playerId);

        return TeamJoinResult.SUCCESS;
    }

    public boolean leave(final UUID playerId, final Team team) {
        Set<UUID> targetMembers = members(team);
        if (!targetMembers.contains(playerId)) {
            return false;
        }

        targetMembers.remove(playerId);
        return true;
    }

    public Set<UUID> membersOf(final Team team) {
        return Collections.unmodifiableSet(members(team));
    }

    private Set<UUID> oppositeMembers(final Team team) {
        return team == Team.WHITE ? blackMembers : whiteMembers;
    }

    private Set<UUID> members(final Team team) {
        return team == Team.WHITE ? whiteMembers : blackMembers;
    }
}
