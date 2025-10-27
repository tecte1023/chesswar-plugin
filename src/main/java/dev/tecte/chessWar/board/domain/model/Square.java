package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.util.BoundingBox;

/**
 * 체스판의 개별 칸 하나를 나타내는 데이터 객체입니다.
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
     * 칸의 영역을 복제하여 반환합니다.
     * 불변성을 보장하기 위해 내부 객체의 복사본을 반환합니다.
     *
     * @return 복제된 {@link BoundingBox} 객체
     */
    @NonNull
    public BoundingBox boundingBox() {
        return boundingBox.clone();
    }
}
