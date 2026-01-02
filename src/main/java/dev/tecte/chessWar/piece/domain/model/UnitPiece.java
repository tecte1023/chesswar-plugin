package dev.tecte.chessWar.piece.domain.model;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * 전장에 존재하는 일반 기물을 나타내는 불변 객체입니다.
 *
 * @param entityId 스폰된 기물 엔티티의 UUID
 * @param spec     기물의 정적 명세 ({@link PieceSpec})
 * @param playerId 기물을 선택한 플레이어의 UUID
 */
public record UnitPiece(UUID entityId, PieceSpec spec, UUID playerId) implements Piece {
    public UnitPiece {
        Objects.requireNonNull(entityId, "Entity id cannot be null");
        Objects.requireNonNull(spec, "Piece specification cannot be null");
    }

    /**
     * 새로운 기물 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param entityId 스폰된 기물 엔티티의 UUID
     * @param spec     기물의 정적 명세
     * @param playerId 기물을 선택한 플레이어의 UUIDx
     * @return 새로운 {@link UnitPiece} 인스턴스
     */
    @NonNull
    public static UnitPiece of(@NonNull UUID entityId, @NonNull PieceSpec spec, @Nullable UUID playerId) {
        return new UnitPiece(entityId, spec, playerId);
    }

    /**
     * 새로운 기물 인스턴스를 생성하는 정적 팩토리 메서드입니다. (플레이어 미선택 상태)
     *
     * @param entityId 스폰된 기물 엔티티의 UUID
     * @param spec     기물의 정적 명세
     * @return 새로운 {@link UnitPiece} 인스턴스
     */
    @NonNull
    public static UnitPiece of(@NonNull UUID entityId, @NonNull PieceSpec spec) {
        return new UnitPiece(entityId, spec, null);
    }

    @Override
    public @NonNull UUID id() {
        return entityId;
    }

    /**
     * 현재 기물이 플레이어에 의해 선택되었는지 여부를 반환합니다.
     *
     * @return 선택되었으면 true, 아니면 false
     */
    public boolean isSelected() {
        return playerId != null;
    }

    /**
     * 지정된 플레이어가 선택한 상태의 새로운 기물을 반환합니다.
     *
     * @param playerId 선택한 플레이어의 UUID
     * @return 플레이어가 설정된 새로운 {@link UnitPiece}
     */
    @NonNull
    public UnitPiece selectedBy(@NonNull UUID playerId) {
        return new UnitPiece(entityId, spec, playerId);
    }
}
