/*
package dev.tecte.chesswar.board;

import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;

public class MoveValidator {

    public boolean canMove(final PieceState state, final Coordinate from, final Coordinate to) {
        if (from.equals(to)) {
            return false;
        }

        final Piece piece = state.piece(from);
        if (piece == null) {
            return false;
        }

        final Piece targetPiece = state.piece(to);
        if (targetPiece != null) {
            if (targetPiece.team() == piece.team()) {
                return false;
            }
        }

        return canReach(state, from, to);
    }

    public boolean canReach(final PieceState state, final Coordinate from, final Coordinate to) {
        if (from.equals(to)) {
            return false;
        }

        final Piece piece = state.piece(from);
        if (piece == null) {
            return false;
        }

        final int dx = to.x() - from.x();
        final int dy = to.y() - from.y();

        return piece.type().policy().canReach(state, from, to, piece, dx, dy);
    }
}
*/
