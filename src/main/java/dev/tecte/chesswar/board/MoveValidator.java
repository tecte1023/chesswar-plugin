package dev.tecte.chesswar.board;

import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.team.Team;

public class MoveValidator {
    public boolean canMove(final PieceState state, final Coordinate from, final Coordinate to) {
        if (!from.isValid() || !to.isValid() || from.equals(to)) {
            return false;
        }

        final Piece piece = state.boardPieces().get(from);
        if (piece == null) {
            return false;
        }

        final Piece targetPiece = state.boardPieces().get(to);
        if (targetPiece != null && targetPiece.team() == piece.team()) {
            return false;
        }

        return canReach(state, from, to);
    }

    public boolean canReach(final PieceState state, final Coordinate from, final Coordinate to) {
        if (!from.isValid() || !to.isValid() || from.equals(to)) {
            return false;
        }

        final Piece piece = state.boardPieces().get(from);
        if (piece == null) {
            return false;
        }

        final Piece targetPiece = state.boardPieces().get(to);
        final int dx = to.x() - from.x();
        final int dy = to.y() - from.y();
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);

        return switch (piece.type()) {
            case KING -> absDx <= 1 && absDy <= 1;
            case QUEEN -> (absDx == absDy || dx == 0 || dy == 0) && isPathClear(state, from, to);
            case ROOK -> (dx == 0 || dy == 0) && isPathClear(state, from, to);
            case BISHOP -> (absDx == absDy) && isPathClear(state, from, to);
            case KNIGHT -> (absDx == 1 && absDy == 2) || (absDx == 2 && absDy == 1);
            case PAWN -> {
                final int direction = (piece.team() == Team.WHITE ? 1 : -1);
                final boolean isForward = dx == 0 && dy == direction;
                final boolean isFirstMove = dx == 0 && dy == 2 * direction && from.y() == (piece.team() == Team.WHITE ? 1 : 6);
                final boolean isCapture = absDx == 1 && dy == direction;

                if (isForward) {
                    yield targetPiece == null;
                }

                if (isFirstMove) {
                    yield targetPiece == null && isPathClear(state, from, to);
                }

                if (isCapture) {
                    yield targetPiece != null;
                }

                yield false;
            }
        };
    }

    private boolean isPathClear(final PieceState state, final Coordinate from, final Coordinate to) {
        final int xDirection = Integer.compare(to.x(), from.x());
        final int yDirection = Integer.compare(to.y(), from.y());
        final int steps = Math.max(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y()));

        for (int i = 1; i < steps; i++) {
            final int currentX = from.x() + (xDirection * i);
            final int currentY = from.y() + (yDirection * i);

            if (state.boardPieces().containsKey(Coordinate.of(currentX, currentY))) {
                return false;
            }
        }

        return true;
    }
}
