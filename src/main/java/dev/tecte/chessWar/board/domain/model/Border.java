package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.util.BoundingBox;

import java.util.Objects;

/**
 * 체스판의 테두리를 나타내는 데이터 객체입니다.
 *
 * @param boundingBox 테두리가 차지하는 영역
 * @param thickness   테두리의 두께
 */
public record Border(BoundingBox boundingBox, int thickness) {
    public Border {
        Objects.requireNonNull(boundingBox, "boundingBox must not be null");
    }

    @NonNull
    public static Border from(@NonNull BoundingBox inside, int thickness) {
        BoundingBox expandedBox = inside.clone().expand(thickness, 0, thickness);

        return new Border(expandedBox, thickness);
    }
}
