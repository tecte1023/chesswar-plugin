package dev.tecte.chessWar.piece.domain.model;

import lombok.NonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * 전장에 참전하여 기물이 된 플레이어를 나타내는 불변 객체입니다.
 * <p>
 * 일반 기물이 플레이어의 선택을 받으면 영웅 기물로 승격됩니다.
 *
 * @param id   참전한 플레이어의 식별자
 * @param spec 기물의 정적 명세
 */
public record HeroPiece(UUID id, PieceSpec spec) implements Piece {
    public HeroPiece {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(spec, "Piece specification cannot be null");
    }

    /**
     * 새로운 영웅 기물을 생성합니다.
     *
     * @param id   참전한 플레이어의 식별자
     * @param spec 기물의 정적 명세
     * @return 생성된 영웅 기물
     */
    @NonNull
    public static HeroPiece of(@NonNull UUID id, @NonNull PieceSpec spec) {
        return new HeroPiece(id, spec);
    }

    @NonNull
    @Override
    public PieceRole role() {
        return PieceRole.HERO;
    }
}
