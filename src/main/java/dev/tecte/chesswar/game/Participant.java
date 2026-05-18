package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;

import java.util.UUID;

public record Participant(UUID playerId, Team team, PieceType pieceType) {
    public static Participant of(UUID playerId, Team team) {
        return new Participant(playerId, team, null);
    }

    public static Participant of(UUID playerId, Team team, PieceType pieceType) {
        return new Participant(playerId, team, pieceType);
    }

    public boolean hasPiece() {
        return pieceType != null;
    }
}
