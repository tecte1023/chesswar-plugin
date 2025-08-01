package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.domain.model.*;
import dev.tecte.chessWar.board.domain.repository.BoardConfigRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@RequiredArgsConstructor
public class BoardFactory {
    private final BoardConfigRepository boardConfigRepository;
    private final OrientationCalculator orientationCalculator;
    private final SquareGridFactory squareGridFactory;

    public Board createBoard(Player player) {
        BoardConfig boardConfig = boardConfigRepository.getBoardConfig();
        Orientation orientation = orientationCalculator.createOrientation(player.getFacing());
        Square[][] squares = squareGridFactory.createSquares(boardConfig, orientation, BoundingBox.of(player.getLocation().getBlock()));
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
