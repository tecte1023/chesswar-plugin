package dev.tecte.chessWar.team.domain.model;

import org.bukkit.OfflinePlayer;

import java.util.Set;

public record TeamInfo(TeamColor color, Set<OfflinePlayer> members, int maxPlayers) {
    public int size() {
        return members.size();
    }
}
