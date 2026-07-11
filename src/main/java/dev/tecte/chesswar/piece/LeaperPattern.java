package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public enum LeaperPattern implements ActionPattern {
    ALL(new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    }),
    KNIGHT_JUMP(new int[][]{
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    });

    private final int[][] offsets;

    @Override
    public void updateAction(
            @NotNull final Piece[] occupancy,
            @NotNull final Piece piece,
            @NotNull final Coordinate position,
            @NotNull final ActionMaskComponent actionMask
    ) {
        long moveMask = 0L;
        long attackMask = 0L;

        for (final int[] offset : offsets) {
            final int cx = position.x() + offset[0];
            final int cy = position.y() + offset[1];

            if (Coordinate.isOutOfBounds(cx, cy)) {
                continue;
            }

            final int index = Coordinate.flatten(cx, cy);
            final Piece occupant = occupancy[index];

            if (occupant == null) {
                moveMask |= (1L << index);
                continue;
            }

            if (occupant.teamSide() != piece.teamSide()) {
                attackMask |= (1L << index);
            }
        }

        actionMask.moveMask(moveMask);
        actionMask.attackMask(attackMask);
    }
}
