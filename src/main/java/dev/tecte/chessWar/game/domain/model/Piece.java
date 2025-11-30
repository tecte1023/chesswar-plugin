package dev.tecte.chessWar.game.domain.model;

import lombok.NonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * 체스판 위에 존재하는 실제 기물 인스턴스를 나타내는 불변 객체입니다.
 *
 * @param entityId 스폰된 기물 엔티티의 UUID.
 * @param spec     기물의 정적 명세 ({@link PieceSpec})
 */
public record Piece(UUID entityId, PieceSpec spec) {
    public Piece {
        Objects.requireNonNull(entityId, "Entity id cannot be null");
        Objects.requireNonNull(spec, "Piece specification cannot be null");
    }

    /**
     * 새로운 기물 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param entityId 스폰된 기물 엔티티의 UUID
     * @param spec     기물의 정적 명세
     * @return 새로운 {@link Piece} 인스턴스
     */
    @NonNull
    public static Piece of(@NonNull UUID entityId, @NonNull PieceSpec spec) {
        return new Piece(entityId, spec);
    }
}
