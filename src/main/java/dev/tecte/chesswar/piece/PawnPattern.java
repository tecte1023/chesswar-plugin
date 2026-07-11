package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.team.TeamSide;
import org.jetbrains.annotations.NotNull;

public enum PawnPattern implements ActionPattern {
    INSTANCE;

    private static final int WHITE_START_ROW = 1;
    private static final int BLACK_START_ROW = 6;

    @Override
    public void updateAction(
            @NotNull final Piece[] occupancy,
            @NotNull final Piece piece,
            @NotNull final Coordinate position,
            @NotNull final ActionMaskComponent actionMask
    ) {
        actionMask.moveMask(calculateMoveMask(occupancy, piece, position));
        actionMask.attackMask(calculateAttackMask(occupancy, piece, position));
    }

    private long calculateMoveMask(
            @NotNull final Piece[] occupancy,
            @NotNull final Piece piece,
            @NotNull final Coordinate position
    ) {
        long moveMask = 0L;

        final TeamSide teamSide = piece.teamSide();
        final int direction = teamSide.direction();
        final int forwardY = position.y() + direction;

        if (Coordinate.isOutOfBounds(position.x(), forwardY)) {
            return moveMask;
        }

        final int forwardIndex = Coordinate.flatten(position.x(), forwardY);

        if (occupancy[forwardIndex] != null) {
            return moveMask;
        }

        moveMask |= (1L << forwardIndex);

        if (hasLeftStartRow(teamSide, position.y())) {
            return moveMask;
        }

        final int doubleY = forwardY + direction;

        if (Coordinate.isOutOfBounds(position.x(), doubleY)) {
            return moveMask;
        }

        final int doubleIndex = Coordinate.flatten(position.x(), doubleY);

        if (occupancy[doubleIndex] == null) {
            return moveMask | (1L << doubleIndex);
        }

        return moveMask;
    }

    private long calculateAttackMask(
            @NotNull final Piece[] occupancy,
            @NotNull final Piece piece,
            @NotNull final Coordinate position
    ) {
        long attackMask = 0L;

        final TeamSide teamSide = piece.teamSide();
        final int forwardY = position.y() + teamSide.direction();

        for (final int dx : new int[]{-1, 1}) {
            final int attackX = position.x() + dx;

            if (Coordinate.isOutOfBounds(attackX, forwardY)) {
                continue;
            }

            final int index = Coordinate.flatten(attackX, forwardY);
            final Piece occupant = occupancy[index];

            if (occupant == null) {
                continue;
            }

            if (occupant.teamSide() == piece.teamSide()) {
                continue;
            }

            attackMask |= (1L << index);
        }

        return attackMask;
    }

    private boolean hasLeftStartRow(@NotNull final TeamSide teamSide, final int currentY) {
        return teamSide == TeamSide.WHITE ? currentY != WHITE_START_ROW : currentY != BLACK_START_ROW;
    }
}
