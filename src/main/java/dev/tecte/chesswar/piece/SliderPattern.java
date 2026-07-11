package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public enum SliderPattern implements ActionPattern {
    STRAIGHT(Vectors.STRAIGHT, false),
    DIAGONAL(Vectors.DIAGONAL, false),
    ALL(Vectors.ALL, false),
    STRAIGHT_LEAP(Vectors.STRAIGHT, true),
    DIAGONAL_LEAP(Vectors.DIAGONAL, true),
    ALL_LEAP(Vectors.ALL, true);

    private static class Vectors {
        private static final int[][] STRAIGHT = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        private static final int[][] DIAGONAL = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        private static final int[][] ALL = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
    }

    private final int[][] directions;
    private final boolean isLeap;

    @Override
    public void updateAction(
            @NotNull final Piece[] occupancy,
            @NotNull final Piece piece,
            @NotNull final Coordinate position,
            @NotNull final ActionMaskComponent actionMask
    ) {
        if (isLeap) {
            updateLeapAction(occupancy, piece, position, actionMask);
        } else {
            updateNormalAction(occupancy, piece, position, actionMask);
        }
    }

    private void updateNormalAction(
            @NotNull final Piece[] occupancy,
            @NotNull final Piece piece,
            @NotNull final Coordinate position,
            @NotNull final ActionMaskComponent actionMask
    ) {
        long moveMask = actionMask.moveMask();
        long attackMask = actionMask.attackMask();

        for (final int[] dir : directions) {
            int cx = position.x() + dir[0];
            int cy = position.y() + dir[1];

            while (Coordinate.isValid(cx, cy)) {
                final int index = Coordinate.flatten(cx, cy);
                final Piece occupant = occupancy[index];

                if (occupant == null) {
                    moveMask |= (1L << index);
                    cx += dir[0];
                    cy += dir[1];
                    continue;
                }

                if (occupant.teamSide() != piece.teamSide()) {
                    attackMask |= (1L << index);
                }

                break;
            }
        }

        actionMask.moveMask(moveMask);
        actionMask.attackMask(attackMask);
    }

    private void updateLeapAction(
            @NotNull final Piece[] occupancy,
            @NotNull final Piece piece,
            @NotNull final Coordinate position,
            @NotNull final ActionMaskComponent actionMask
    ) {
        long moveMask = actionMask.moveMask();
        long attackMask = actionMask.attackMask();

        for (final int[] dir : directions) {
            int cx = position.x() + dir[0];
            int cy = position.y() + dir[1];
            boolean leaped = false;

            while (Coordinate.isValid(cx, cy)) {
                final int index = Coordinate.flatten(cx, cy);
                final Piece occupant = occupancy[index];

                if (occupant == null) {
                    moveMask |= (1L << index);
                    cx += dir[0];
                    cy += dir[1];
                    continue;
                }

                if (leaped) {
                    break;
                }

                if (occupant.teamSide() == piece.teamSide()) {
                    leaped = true;
                    cx += dir[0];
                    cy += dir[1];
                    continue;
                }

                attackMask |= (1L << index);
                break;
            }
        }

        actionMask.moveMask(moveMask);
        actionMask.attackMask(attackMask);
    }
}
