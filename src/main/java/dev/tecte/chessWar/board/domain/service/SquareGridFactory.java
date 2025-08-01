package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.Square;
import dev.tecte.chessWar.board.domain.model.SquareColor;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class SquareGridFactory {
    public Square[][] createSquares(SquareConfig squareConfig, Orientation orientation, BoundingBox boundingBox) {
        int rowCount = squareConfig.rowCount();
        int columnCount = squareConfig.columnCount();
        int width = squareConfig.width();
        int height = squareConfig.height();
        Square[][] squares = new Square[rowCount][columnCount];

        Vector forward = orientation.forward();
        Vector left = orientation.left();
        Vector right = orientation.right();

        boundingBox.shift(forward)
                .shift(left.clone().multiply(width * columnCount / 2))
                .expand(right, width)
                .expand(forward, height);

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

        return squares;
    }
}
