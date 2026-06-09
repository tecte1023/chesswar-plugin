package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.piece.PieceType;

public class CombatPolicy {
    public boolean canAttack(final Piece attacker, final Piece target) {
        if (attacker.team() != target.team()) {
            return true;
        }

        if (attacker.type() == PieceType.BISHOP) {
            return true;
        }

        return canCommand(attacker, target);
    }

    public boolean canCommand(final Piece commander, final Piece target) {
        if (commander.team() != target.team()) {
            return false;
        }

        return commander.type() == PieceType.KING && !target.isPlayerPiece();
    }

    public Piece getPrimaryAttacker(final Participant participant, final PieceState state) {
        final Coordinate kingCoord = state.entityToCoordinate().get(participant.playerId());

        if (kingCoord != null) {
            final Piece king = state.boardPieces().get(kingCoord);
            if (king != null && king.type() == PieceType.KING) {
                return king;
            }
        }

        return Piece.of(participant.playerId(), participant.team(), PieceType.PAWN);
    }

    public boolean isWinConditionMet(final Piece killedPiece) {
        return killedPiece.type() == PieceType.KING;
    }
}
