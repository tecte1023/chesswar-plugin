package dev.tecte.chessWar.board.domain.model;

import java.util.Objects;

/**
 * 체스판의 전체 구조를 나타내는 레코드입니다.
 *
 * @param squareGrid  체스판의 격자
 * @param innerBorder 체스판의 내부 테두리
 * @param frame       체스판의 외부 프레임
 */
public record Board(
        SquareGrid squareGrid,
        Border innerBorder,
        Border frame
) {
    public Board {
        Objects.requireNonNull(squareGrid, "squareGrid cannot be null");
        Objects.requireNonNull(innerBorder, "innerBorder cannot be null");
        Objects.requireNonNull(frame, "frame cannot be null");
    }
}
