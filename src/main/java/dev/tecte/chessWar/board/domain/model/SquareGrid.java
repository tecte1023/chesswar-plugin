package dev.tecte.chessWar.board.domain.model;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record SquareGrid(
        Square[][] squares
) {
    @Contract("_, _, _ -> new")
    public static @NotNull SquareGrid from(@NotNull SquareConfig squareConfig, @NotNull Orientation orientation, @NotNull BoundingBox boundingBox) {
        int rowCount = squareConfig.rowCount();
        int columnCount = squareConfig.columnCount();
        int width = squareConfig.width();
        int height = squareConfig.height();

        Vector forward = orientation.forward();
        Vector right = orientation.right();

        Square[][] squares = new Square[rowCount][columnCount];

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                Vector squareOffset = forward.clone().multiply(row * height)
                        .add(right.clone().multiply(col * width));
                BoundingBox squareBoundingBox = boundingBox.clone().shift(squareOffset);

                squares[row][col] = Square.builder()
                        .logicalRow(row)
                        .logicalCol(col)
                        .boundingBox(squareBoundingBox)
                        .color((row + col) % 2 == 0 ? SquareColor.BLACK : SquareColor.WHITE)
                        .build();
            }
        }

        return new SquareGrid(squares);
    }

    @Contract("-> !null")
    public @NotNull BoundingBox getBoundingBox() {
        BoundingBox boundingBox = squares[0][0].boundingBox().clone();

        for (Square[] row : squares) {
            for (Square square : row) {
                boundingBox.union(square.boundingBox());
            }
        }

        return boundingBox;
    }

    @Contract("_, _ -> !null")
    public Square getSquareAt(int row, int col) {
        return squares[row][col];
    }
}
