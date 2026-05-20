package dev.tecte.chesswar.board;

import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MoveValidator {
    private final GameManager gameManager;

    public boolean canMove(Coordinate from, Coordinate to) {
        if (!from.isValid() || !to.isValid() || from.equals(to)) {
            return false;
        }

        Piece piece = gameManager.findPieceAt(from).orElse(null);

        if (piece == null) {
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
            case PAWN -> dx == 0 && dy == (piece.team() == Team.WHITE ? 1 : -1);
        };
    }

    private boolean isPathClear(Coordinate from, Coordinate to) {
        int xDirection = Integer.compare(to.x(), from.x());
        int yDirection = Integer.compare(to.y(), from.y());
        int currentX = from.x() + xDirection;
        int currentY = from.y() + yDirection;

        while (currentX != to.x() || currentY != to.y()) {
            if (gameManager.findPieceAt(Coordinate.of(currentX, currentY)).isPresent()) {
                return false;
            }

            currentX += xDirection;
            currentY += yDirection;
        }

        return true;
    }
}
