package dev.tecte.chesswar.board;

import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MoveValidator {
    private final GameManager gameManager;
    private final PieceManager pieceManager;

    public boolean canMove(Coordinate from, Coordinate to) {
        if (!from.isValid() || !to.isValid() || from.equals(to)) {
            return false;
        }

        Piece piece = pieceManager.findPieceAt(from).orElse(null);

        if (piece == null) {
            return false;
        }

        Piece targetPiece = pieceManager.findPieceAt(to).orElse(null);
        if (targetPiece != null && targetPiece.team() == piece.team()) {
            return false;
        }

        int dx = to.x() - from.x();
        int dy = to.y() - from.y();
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        return switch (piece.type()) {
            case KING -> absDx <= 1 && absDy <= 1;
            case QUEEN -> (absDx == absDy || dx == 0 || dy == 0) && isPathClear(from, to);
            case ROOK -> (dx == 0 || dy == 0) && isPathClear(from, to);
            case BISHOP -> (absDx == absDy) && isPathClear(from, to);
            case KNIGHT -> (absDx == 1 && absDy == 2) || (absDx == 2 && absDy == 1);
            case PAWN -> {
                int direction = (piece.team() == Team.WHITE ? 1 : -1);
                boolean isForward = dx == 0 && dy == direction;
                boolean isFirstMove = dx == 0 && dy == 2 * direction && from.y() == (piece.team() == Team.WHITE ? 1 : 6);
                boolean isCapture = absDx == 1 && dy == direction;

                if (isForward) {
                    yield pieceManager.findPieceAt(to).isEmpty();
                }

                if (isFirstMove) {
                    yield pieceManager.findPieceAt(to).isEmpty() && isPathClear(from, to);
                }

                if (isCapture) {
                    yield pieceManager.findPieceAt(to).isPresent();
                }

                yield false;
            }
        };
    }

    private boolean isPathClear(Coordinate from, Coordinate to) {
        int xDirection = Integer.compare(to.x(), from.x());
        int yDirection = Integer.compare(to.y(), from.y());
        int steps = Math.max(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y()));

        for (int i = 1; i < steps; i++) {
            int currentX = from.x() + (xDirection * i);
            int currentY = from.y() + (yDirection * i);

            if (pieceManager.findPieceAt(Coordinate.of(currentX, currentY)).isPresent()) {
                return false;
            }
        }

        return true;
    }
}
