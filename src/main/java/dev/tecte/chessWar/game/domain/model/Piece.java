package dev.tecte.chessWar.game.domain.model;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 체스판 위에 존재하는 실제 기물 인스턴스를 나타내는 불변 객체입니다.
 *
 * @param entityId 스폰된 기물 엔티티의 UUID.
 * @param spec     기물의 정적 명세 ({@link PieceSpec})
 * @param playerId 해당 기물을 선택하여 참전한 플레이어의 UUID
 */
public record Piece(UUID entityId, PieceSpec spec, UUID playerId) {
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
        return new Piece(entityId, spec, null);
    }

    /**
     * 새로운 기물 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param entityId 스폰된 기물 엔티티의 UUID
     * @param spec     기물의 정적 명세
     * @param playerId 해당 기물을 선택하여 참전한 플레이어의 UUID
     * @return 새로운 {@link Piece} 인스턴스
     */
    @NonNull
    public static Piece of(@NonNull UUID entityId, @NonNull PieceSpec spec, @Nullable UUID playerId) {
        return new Piece(entityId, spec, playerId);
    }

    /**
     * 해당 기물을 특정 플레이어가 선택하여 참전한 상태로 설정한 새로운 기물 객체를 반환합니다.
     *
     * @param playerId 이 기물로 참전할 플레이어의 UUID
     * @return 플레이어가 참전한 상태의 새로운 {@link Piece} 객체
     */
    @NonNull
    public Piece selectedBy(@NonNull UUID playerId) {
        return new Piece(entityId, spec, playerId);
    }

    /**
     * 이 기물로 참전 중인 플레이어가 있는지 확인합니다.
     *
     * @return 참전 중인 플레이어가 있으면 true, 아니면 false
     */
    public boolean isSelected() {
        return playerId != null;
    }

    /**
     * 이 기물로 참전 중인 플레이어의 ID를 {@link Optional}로 반환합니다.
     *
     * @return 플레이어 UUID를 담은 {@link Optional}, 참전 중인 플레이어가 없으면 빈 {@link Optional}
     */
    @NonNull
    public Optional<UUID> player() {
        return Optional.ofNullable(playerId);
    }
}
