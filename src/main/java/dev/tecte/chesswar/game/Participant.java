package dev.tecte.chesswar.game;

import dev.tecte.chesswar.team.Team;
import java.util.UUID;

public record Participant(UUID playerId, Team team) {
    public static Participant of(UUID playerId, Team team) {
        return new Participant(playerId, team);
    }
}
