package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.piece.ability.PieceAbility;
import dev.tecte.chesswar.team.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Piece {
    private final UUID ownerId;
    private final Team team;
    private final PieceType type;
    private final List<PieceAbility> abilities = new ArrayList<>();

    private double currentHealth;

    public static Piece of(Team team, PieceType type) {
        return new Piece(null, team, type, type.baseHealth());
    }

    public static Piece of(UUID ownerId, Team team, PieceType type) {
        return new Piece(ownerId, team, type, type.baseHealth());
    }

    public void addAbility(final PieceAbility ability) {
        abilities.add(ability);
    }

    public boolean isPlayerPiece() {
        return ownerId != null;
    }
}
