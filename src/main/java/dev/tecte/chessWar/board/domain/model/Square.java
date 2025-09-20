package dev.tecte.chessWar.board.domain.model;

import org.bukkit.util.BoundingBox;

import java.util.Objects;

/**
 * 체스판의 개별 칸 하나를 나타내는 데이터 객체입니다.
 *
 * @param boundingBox 칸의 영역
 * @param color       칸의 색상
 */
public record Square(BoundingBox boundingBox, SquareColor color) {
    public Square {
        Objects.requireNonNull(boundingBox, "boundingBox must not be null");
        Objects.requireNonNull(color, "color must not be null");
    }
}
