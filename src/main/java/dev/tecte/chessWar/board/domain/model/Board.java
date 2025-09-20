package dev.tecte.chessWar.board.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * 체스판 하나를 나타내는 도메인 모델의 최상위 애그리거트입니다.
 * 체스판의 고유 ID, 격자, 테두리 등 체스판을 구성하는 모든 요소를 포함합니다.
 *
 * @param id          체스판의 고유 식별자
 * @param squareGrid  체스판의 격자 영역
 * @param innerBorder 내부 테두리
 * @param frame       외부 테두리
 */
public record Board(
        UUID id,
        SquareGrid squareGrid,
        Border innerBorder,
        Border frame
) {
    public Board {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(squareGrid, "squareGrid must not be null");
        Objects.requireNonNull(innerBorder, "innerBorder must not be null");
        Objects.requireNonNull(frame, "frame must not be null");
    }
}
