package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.util.BoundingBox;

import java.util.Objects;

/**
 * 체스판의 테두리를 나타내는 불변 객체입니다.
 *
 * @param borderType  테두리의 종류
 * @param boundingBox 테두리가 차지하는 영역
 * @param thickness   테두리의 두께
 */
public record Border(
        BorderType borderType,
        BoundingBox boundingBox,
        int thickness
) {
    public Border {
        Objects.requireNonNull(borderType, "Border type cannot be null");
        Objects.requireNonNull(boundingBox, "Bounding box cannot be null");

        if (thickness < 0) {
            throw new IllegalArgumentException("Thickness cannot be negative.");
        }

        boundingBox = boundingBox.clone();
    }

    /**
     * 내부 영역을 기준으로 테두리를 생성합니다.
     *
     * @param borderType 테두리의 종류
     * @param inside     테두리가 감쌀 내부 영역
     * @param thickness  테두리의 두께
     * @return 생성된 테두리
     */
    @NonNull
    public static Border from(@NonNull BorderType borderType, @NonNull BoundingBox inside, int thickness) {
        BoundingBox expandedBox = inside.clone().expand(thickness, 0, thickness);

        return new Border(borderType, expandedBox, thickness);
    }

    /**
     * 테두리의 영역을 반환합니다.
     *
     * @return 복제된 영역
     */
    @NonNull
    public BoundingBox boundingBox() {
        return boundingBox.clone();
    }
}
