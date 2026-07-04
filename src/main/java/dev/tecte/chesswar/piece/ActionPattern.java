package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import org.jetbrains.annotations.NotNull;

public interface ActionPattern {
    void updateAction(
            @NotNull Piece[] occupancy,
            @NotNull Piece piece,
            @NotNull Coordinate position,
            @NotNull ActionMaskComponent actionMask
    );
}
