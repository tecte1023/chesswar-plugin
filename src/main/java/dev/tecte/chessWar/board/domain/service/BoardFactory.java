package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.*;
import dev.tecte.chessWar.board.domain.repository.BoardConfigRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

@RequiredArgsConstructor
public class BoardFactory {
    private final BoardConfigRepository boardConfigRepository;
    private final OrientationCalculator orientationCalculator;
    private final SquareGridFactory squareGridFactory;

    public Board createBoard(Location location) {
        BoardConfig boardConfig = boardConfigRepository.getBoardConfig();
        Orientation orientation = orientationCalculator.createOrientation(location.getYaw());
        Square[][] squares = squareGridFactory.createSquares(boardConfig.squareConfig(), orientation, BoundingBox.of(location.getBlock()));
        Border innerBorder = createBorder(calculateBoundingBox(squares), boardConfig.innerBorderConfig().thickness());
        Border frame = createBorder(innerBorder.boundingBox(), boardConfig.frameConfig().thickness());

        return Board.builder()
                .squares(squares)
                .innerBorder(innerBorder)
                .frame(frame)
                .build();
    }

    private BoundingBox calculateBoundingBox(Square[][] squares) {
        BoundingBox boundingBox = squares[0][0].boundingBox().clone();

        for (Square[] row : squares) {
            for (Square square : row) {
                boundingBox.union(square.boundingBox());
            }
        }

        return boundingBox;
    }

    private Border createBorder(BoundingBox boundingBox, int thickness) {
        BoundingBox borderBoundingBox = boundingBox.clone().expand(thickness, 0, thickness);

        return new Border(borderBoundingBox, thickness);
    }
}
