package dev.tecte.chessWar.board.domain.model;

import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * 체스판 생성 파라미터를 정의하는 불변 객체입니다.
 *
 * @param worldName   체스판 생성 월드의 이름
 * @param gridAnchor  격자의 기준점 (a1: 좌측 하단 모서리)
 * @param orientation 격자의 방향
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
}
