package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.GridPoint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PieceType {
    PAWN(60, 15, false),
    KNIGHT(100, 25, true),
    BISHOP(100, 25, false),
    ROOK(160, 20, false),
    QUEEN(140, 35, false),
    KING(260, 10, false);

    private final int maxHp;
    private final int atk;
    private final boolean jumps;

    public boolean canMove(final Team team, final GridPoint from, final GridPoint to) {
        int dx = to.x() - from.x();
        int dy = to.y() - from.y();
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        return switch (this) {
            case PAWN -> canPawnMove(team, from.y(), dx, dy);
            case KNIGHT -> isKnightMove(absDx, absDy);
            case BISHOP -> isDiagonal(absDx, absDy);
            case ROOK -> isStraight(absDx, absDy);
            case QUEEN -> isStraight(absDx, absDy) || isDiagonal(absDx, absDy);
            case KING -> isKingMove(absDx, absDy);
        };
    }

    private boolean canPawnMove(final Team team, final int fromY, final int dx, final int dy) {
        if (dx != 0) {
            return false;
        }

        int forwardDistance = dy * team.forwardStep();

        return forwardDistance == 1 || (fromY == team.pawnRank() && forwardDistance == 2);
    }

    private boolean isKnightMove(final int absDx, final int absDy) {
        return (absDx == 1 && absDy == 2) || (absDx == 2 && absDy == 1);
    }

    private boolean isStraight(final int absDx, final int absDy) {
        return (absDx == 0 && absDy != 0) || (absDy == 0 && absDx != 0);
    }

    private boolean isDiagonal(final int absDx, final int absDy) {
        return absDx == absDy && absDx != 0;
    }

    private boolean isKingMove(final int absDx, final int absDy) {
        return Math.max(absDx, absDy) == 1;
    }

    public boolean canAttack(final Team team, final GridPoint from, final GridPoint to) {
        return this == PAWN ? canPawnAttack(team, from, to) : canMove(team, from, to);
    }

    private boolean canPawnAttack(final Team team, final GridPoint from, final GridPoint to) {
        int absDx = Math.abs(to.x() - from.x());
        int forwardDistance = (to.y() - from.y()) * team.forwardStep();
        return forwardDistance == 1 && absDx == 1;
    }
}
