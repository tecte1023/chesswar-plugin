package dev.tecte.chesswar.game.component;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "of")
public class Participant {
    private final UUID playerId;
    private final String playerName;
    private final Team team;
    private final Statistics statistics;
    private Coordinate initialCoordinate;
    private PieceType selectedType;
    private boolean ready;
    private int turnOrder;
    private Coordinate commanderTarget;
    private org.bukkit.GameMode originalGameMode;
    private Double originalHealth;
    private Double originalAttackDamage;
    private int gold;
    private final List<String> statusEffects;

    public static Participant of(UUID playerId, String playerName, Team team, org.bukkit.GameMode originalGameMode) {
        return new Participant(playerId, playerName, team, new Statistics(), null, null, false, -1, null, originalGameMode, null, null, 0, new ArrayList<>());
    }

    public static Participant of(UUID playerId, String playerName, Team team, Coordinate coordinate, org.bukkit.GameMode originalGameMode) {
        return new Participant(playerId, playerName, team, new Statistics(), coordinate, null, false, -1, null, originalGameMode, null, null, 0, new ArrayList<>());
    }

    public boolean hasPiece() {
        return initialCoordinate != null;
    }
}
