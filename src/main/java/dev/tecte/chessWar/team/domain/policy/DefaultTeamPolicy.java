package dev.tecte.chessWar.team.domain.policy;

import jakarta.inject.Singleton;

@Singleton
public class DefaultTeamPolicy implements TeamPolicy {
    public static final int MIN_MAX_PLAYERS = 1;
    public static final int MAX_MAX_PLAYERS = 8;
    private static final int DEFAULT_MAX_PLAYERS = 8;

    @Override
    public int applyTo(int value) {
        if (value < MIN_MAX_PLAYERS || value > MAX_MAX_PLAYERS) {
            return DEFAULT_MAX_PLAYERS;
        }

        return value;
    }

    @Override
    public int getDefaultMaxPlayers() {
        return DEFAULT_MAX_PLAYERS;
    }
}
