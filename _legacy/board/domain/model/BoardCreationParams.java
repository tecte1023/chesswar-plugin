package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * 체스판 구축에 필요한 초기 설정 데이터를 관리합니다.
 *
 * @param worldName   체스판이 생성될 월드 이름
 * @param gridAnchor  격자의 기준점
 * @param orientation 체스판의 정면이 향할 방위
 */
public record BoardCreationParams(
        String worldName,
        Vector gridAnchor,
        Orientation orientation
) {
    public BoardCreationParams {
        Objects.requireNonNull(worldName, "World name cannot be null.");
        Objects.requireNonNull(gridAnchor, "Grid anchor cannot be null.");
        Objects.requireNonNull(orientation, "Orientation cannot be null.");
    }

    /**
     * 체스판 설정 파라미터를 생성합니다.
     *
     * @param worldName   대상 월드 이름
     * @param gridAnchor  격자 기준점 위치
     * @param orientation 체스판 배치 방향
     * @return 설정 파라미터
     */
    @NonNull
    public static BoardCreationParams of(
            @NonNull String worldName,
            @NonNull Vector gridAnchor,
            @NonNull Orientation orientation
    ) {
        return new BoardCreationParams(worldName, gridAnchor, orientation);
    }
}
