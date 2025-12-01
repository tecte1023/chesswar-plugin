package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;
import lombok.NonNull;
import org.bukkit.util.Vector;

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

    /**
     * 지정된 좌표에 대한 기물의 스폰 위치 벡터를 계산합니다.
     *
     * @param coordinate 스폰할 칸의 좌표
     * @return 스폰 위치를 나타내는 {@link Vector} 객체
     */
    @NonNull
    public Vector spawnPositionVector(@NonNull Coordinate coordinate) {
        return squareGrid.squareAt(coordinate)
                .boundingBox()
                .getCenter()
                .add(new Vector(0, 1, 0));
    }

    /**
     * 보드의 방향 정보를 반환합니다.
     *
     * @return 보드의 {@link Orientation}
     */
    @NonNull
    public Orientation orientation() {
        return squareGrid.orientation();
    }

    /**
     * 보드 전체 영역의 중심점 위치 벡터를 반환합니다.
     *
     * @return 보드의 중심점 {@link Vector}
     */
    @NonNull
    public Vector center() {
        return squareGrid.boundingBox().getCenter();
    }
}
