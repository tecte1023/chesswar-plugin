package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.UUID;

/**
 * 체스판({@link Board}) 객체를 생성하는 팩토리 클래스입니다.
 */
@Singleton
public class BoardFactory {
    /**
     * 주어진 생성 명세({@link BoardCreationSpec})를 바탕으로 체스판 객체를 생성합니다.
     * 내부적으로 격자, 테두리 등을 모두 계산하고 조립합니다.
     *
     * @param spec 체스판 생성에 필요한 모든 정보
     * @return 생성된 {@link Board} 인스턴스
     */
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
