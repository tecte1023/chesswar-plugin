package dev.tecte.chessWar.board.domain.model;

import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * 체스판 생성을 위한 동적 파라미터들을 담는 객체입니다.
 *
 * @param gridAnchor  격자의 기준점 (a1: 좌측 하단 모서리)
 * @param orientation 격자의 방향
 */
public record BoardCreationParams(
        Vector gridAnchor,
        Orientation orientation
) {
    public BoardCreationParams {
        Objects.requireNonNull(gridAnchor, "Grid anchor cannot be null.");
        Objects.requireNonNull(orientation, "Orientation cannot be null.");
    }
}
