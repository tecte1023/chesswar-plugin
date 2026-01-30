package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardCreationParams;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.BorderType;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.board.domain.model.spec.BorderSpec;
import dev.tecte.chessWar.board.domain.model.spec.GridSpec;
import dev.tecte.chessWar.board.domain.model.spec.SquareSpec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 체스판을 생성하는 팩토리 클래스입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardFactory {
    private final GridSpec gridSpec;
    private final SquareSpec squareSpec;
    private final BorderSpec borderSpec;

    /**
     * 주어진 파라미터로 체스판을 생성합니다.
     *
     * @param params 체스판 생성 파라미터
     * @return 생성된 체스판
     */
    @NonNull
    public Board createBoard(@NonNull BoardCreationParams params) {
        SquareGrid squareGrid = SquareGrid.builder()
                .anchor(params.gridAnchor())
                .orientation(params.orientation())
                .gridSpec(gridSpec)
                .squareSpec(squareSpec)
                .build();
        Border innerBorder = Border.from(BorderType.INNER_BORDER, squareGrid.boundingBox(), borderSpec.innerThickness());
        Border frame = Border.from(BorderType.FRAME, innerBorder.boundingBox(), borderSpec.frameThickness());

        return Board.builder()
                .worldName(params.worldName())
                .squareGrid(squareGrid)
                .innerBorder(innerBorder)
                .frame(frame)
                .build();
    }
}
