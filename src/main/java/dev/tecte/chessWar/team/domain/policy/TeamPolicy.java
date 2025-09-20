package dev.tecte.chessWar.team.domain.policy;

public interface TeamPolicy {
    int applyTo(int value);

    int getDefaultMaxPlayers();
}
