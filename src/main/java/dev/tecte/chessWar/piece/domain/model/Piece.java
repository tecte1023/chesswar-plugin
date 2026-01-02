package dev.tecte.chessWar.piece.domain.model;

import lombok.NonNull;

import java.util.UUID;

/**
 * 전장에서 전투를 수행하는 모든 기물을 나타냅니다.
 * <p>
 * 일반 기물이나 영웅 기물 모두 하나의 기물로 식별하고 관리합니다.
 */
public interface Piece {
    /**
     * 기물의 고유 식별자를 반환합니다.
     *
     * @return 기물의 UUID
     */
    @NonNull
    UUID id();

    /**
     * 기물의 정적 명세를 반환합니다.
     *
     * @return 기물의 정적 명세 ({@link PieceSpec})
     */
    @NonNull
    PieceSpec spec();
}
