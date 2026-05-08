package dev.tecte.chessWar.piece.domain.model;

import lombok.NonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * 전장에 존재하는 일반 기물을 나타내는 불변 객체입니다.
 *
 * @param id   기물의 식별자
 * @param spec 기물의 정적 명세
 */
public record UnitPiece(UUID id, PieceSpec spec) implements Piece {
    public UnitPiece {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(spec, "Piece specification cannot be null");
    }

    /**
     * 새로운 일반 기물을 생성합니다.
     *
     * @param id   기물의 식별자
     * @param spec 기물의 정적 명세
     * @return 생성된 일반 기물
     */
    @NonNull
    public static UnitPiece of(@NonNull UUID id, @NonNull PieceSpec spec) {
        return new UnitPiece(id, spec);
    }

    @NonNull
    @Override
    public PieceRole role() {
        return PieceRole.UNIT;
    }

    @Override
    public boolean isSelectable() {
        return spec.type() != PieceType.PAWN;
    }
}
