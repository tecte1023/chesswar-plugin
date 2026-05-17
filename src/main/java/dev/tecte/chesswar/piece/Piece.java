package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.team.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Piece {
    private final UUID ownerId;
    private final Team team;
    private final PieceType type;

    private double currentHp;

    public static Piece of(Team team, PieceType type) {
        return new Piece(null, team, type, type.baseHp());
    }

    public static Piece of(UUID ownerId, Team team, PieceType type) {
        return new Piece(ownerId, team, type, type.baseHp());
    }

    public boolean isPlayerPiece() {
        return ownerId != null;
    }
}
