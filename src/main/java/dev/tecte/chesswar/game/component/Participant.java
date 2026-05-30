package dev.tecte.chesswar.game.component;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.team.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "of")
public class Participant {
    private final UUID playerId;
    private final Team team;
    private final Statistics statistics;
    private Coordinate initialCoordinate;
    private boolean ready;
    private int turnOrder;
    private Coordinate commanderTarget;

    public static Participant of(UUID playerId, Team team) {
        return new Participant(playerId, team, new Statistics(), null, false, -1, null);
    }

    public static Participant of(UUID playerId, Team team, Coordinate coordinate) {
        return new Participant(playerId, team, new Statistics(), coordinate, false, -1, null);
    }

    public boolean hasPiece() {
        return initialCoordinate != null;
    }
}
