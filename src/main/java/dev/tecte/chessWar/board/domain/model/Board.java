package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;

import java.util.Objects;

/**
 * 체스판의 구조를 나타내는 불변 데이터 객체입니다.
 */
@Builder
public record Board(
        String worldName,
        SquareGrid squareGrid,
        Border innerBorder,
        Border frame
) {
    public Board {
        Objects.requireNonNull(worldName, "World name cannot be null");
        Objects.requireNonNull(squareGrid, "Square grid cannot be null");
        Objects.requireNonNull(innerBorder, "Inner border cannot be null");
        Objects.requireNonNull(frame, "Frame cannot be null");
    }
}
