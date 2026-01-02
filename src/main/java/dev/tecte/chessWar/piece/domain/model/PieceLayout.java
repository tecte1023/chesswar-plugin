package dev.tecte.chessWar.piece.domain.model;

import dev.tecte.chessWar.board.domain.model.Coordinate;
import lombok.NonNull;

import java.util.Map;
import java.util.Objects;

/**
 * 체스판에 배치된 모든 기물 설계도의 묶음입니다.
 *
 * @param pieces 좌표에 따라 정리된 기물 설계도의 맵
 */
public record PieceLayout(Map<Coordinate, PieceSpec> pieces) {
    public PieceLayout {
        Objects.requireNonNull(pieces, "pieces cannot be null");
        pieces = Map.copyOf(pieces);
    }

    /**
     * 기물 배치 정보를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param pieces 좌표에 따라 정리된 기물 설계도의 맵
     * @return 생성된 {@link PieceLayout} 인스턴스
     */
    @NonNull
    public static PieceLayout of(@NonNull Map<Coordinate, PieceSpec> pieces) {
        return new PieceLayout(pieces);
    }
}
