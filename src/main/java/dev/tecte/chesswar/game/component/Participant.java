package dev.tecte.chesswar.game.component;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.team.Team;

import java.util.UUID;

public record Participant(UUID playerId, Team team, Coordinate initialCoordinate) {
    public static Participant of(UUID playerId, Team team) {
        return new Participant(playerId, team, null);
    }

    public static Participant of(UUID playerId, Team team, Coordinate coordinate) {
        return new Participant(playerId, team, coordinate);
    }

    public boolean hasPiece() {
        return initialCoordinate != null;
    }
}
