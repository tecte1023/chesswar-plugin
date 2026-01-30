package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.util.BoundingBox;

/**
 * 체스판의 개별 칸을 나타내는 불변 객체입니다.
 *
 * @param color       칸의 색상
 * @param boundingBox 칸의 영역
 */
public record Square(SquareColor color, BoundingBox boundingBox) {
    public Square(@NonNull SquareColor color, @NonNull BoundingBox boundingBox) {
        this.color = color;
        this.boundingBox = boundingBox.clone();
    }

    /**
     * 칸의 영역을 반환합니다.
     *
     * @return 복제된 영역
     */
    @NonNull
    public BoundingBox boundingBox() {
        return boundingBox.clone();
    }
}
