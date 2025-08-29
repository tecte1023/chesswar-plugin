package dev.tecte.chessWar.board.domain.model;

import lombok.Getter;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Getter
public final class SquareGrid {
    private final Vector anchor;
    private final Orientation orientation;
    private final int rowCount;
    private final int colCount;
    private final int squareWidth;
    private final int squareHeight;
    private final Vector colStep;
    private final Vector rowStep;

    private SquareGrid(
            @NotNull Vector anchor,
            @NotNull Orientation orientation,
            int rowCount,
            int colCount,
            int squareWidth,
            int squareHeight
    ) {
        this.anchor = anchor;
        this.orientation = orientation;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.squareWidth = squareWidth;
        this.squareHeight = squareHeight;
        colStep = orientation.getRight().clone().multiply(squareWidth);
        rowStep = orientation.getForward().clone().multiply(squareHeight);
    }

    @NotNull
    public static SquareGrid create(
            @NotNull Vector anchor,
            @NotNull Orientation orientation,
            int rowCount,
            int colCount,
            int squareWidth,
            int squareHeight
    ) {
        return new SquareGrid(
                anchor,
                orientation,
                rowCount,
                colCount,
                squareWidth,
                squareHeight
        );
    }

    @NotNull
    public BoundingBox getBoundingBox() {
        Vector diagonalOffset = colStep.clone().multiply(colCount)
                .add(rowStep.clone().multiply(rowCount));
        Vector diagonalCorner = anchor.clone().add(diagonalOffset);

        return BoundingBox.of(anchor, diagonalCorner);
    }

    @NotNull
    public Square getSquareAt(int row, int col) {
        Vector squareOrigin = anchor.clone()
                .add(colStep.clone().multiply(col))
                .add(rowStep.clone().multiply(row));
        Vector diagonalCorner = squareOrigin.clone().add(colStep).add(rowStep);
        BoundingBox boundingBox = BoundingBox.of(squareOrigin, diagonalCorner);
        SquareColor color = (row + col) % 2 == 0 ? SquareColor.BLACK : SquareColor.WHITE;

        return new Square(boundingBox, color);
    }
}
