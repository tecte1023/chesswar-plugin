package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class SquareGridFactory {
    public Square[][] createSquares(BoardConfig config, Orientation orientation, BoundingBox boundingBox) {
        SquareConfig squareConfig = config.squareConfig();
        int innerBorderThickness = config.innerBorderConfig().thickness();
        int frameThickness = config.frameConfig().thickness();
        int rowCount = squareConfig.rowCount();
        int columnCount = squareConfig.columnCount();
        int width = squareConfig.width();
        int height = squareConfig.height();
        Square[][] squares = new Square[rowCount][columnCount];

        Vector forward = orientation.forward();
        Vector left = orientation.left();
        Vector right = orientation.right();

        boundingBox.shift(forward.clone().multiply(innerBorderThickness + frameThickness + 1))
                .shift(left.clone().multiply(width * columnCount / 2))
                .expand(right, width - 1)
                .expand(forward, height - 1);

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
