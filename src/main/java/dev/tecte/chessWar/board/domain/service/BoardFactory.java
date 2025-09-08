package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.UUID;

@Singleton
public class BoardFactory {
    @NonNull
    public Board createBoard(@NonNull BoardCreationSpec spec) {
        SquareGrid squareGrid = SquareGrid.create(
                spec.gridAnchor(),
                spec.orientation(),
                spec.squareConfig().rowCount(),
                spec.squareConfig().colCount(),
                spec.squareConfig().width(),
                spec.squareConfig().height()
        );
        Border innerBorder = Border.from(squareGrid.getBoundingBox(), spec.innerBorderConfig().thickness());
        Border frame = Border.from(innerBorder.boundingBox(), spec.frameConfig().thickness());

        return Board.builder()
                .id(UUID.randomUUID())
                .squareGrid(squareGrid)
                .innerBorder(innerBorder)
                .frame(frame)
                .build();
    }
}
