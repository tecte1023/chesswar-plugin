package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.util.BoundingBox;

/**
 * 체스판의 테두리를 나타내는 데이터 객체입니다.
 *
 * @param boundingBox 테두리가 차지하는 영역
 * @param thickness   테두리의 두께
 */
public record Border(BoundingBox boundingBox, int thickness) {
    /**
     * 주어진 영역을 지정된 두께만큼 확장하여 새로운 테두리를 생성합니다.
     *
     * @param inside    확장의 기준이 될 영역
     * @param thickness 영역을 확장할 두께
     * @return 생성된 테두리 객체
     */
    @NonNull
    public static Border from(@NonNull BoundingBox inside, int thickness) {
        BoundingBox expandedBox = inside.clone().expand(thickness, 0, thickness);

        return new Border(expandedBox, thickness);
    }
}
