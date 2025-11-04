package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.board.domain.model.Coordinate;

import java.util.Map;
import java.util.Objects;

/**
 * 체스판에 배치된 모든 기물 설계도의 묶음입니다.
 * 이 객체는 불변입니다.
 *
 * @param pieces 좌표에 따라 정리된 기물 설계도의 맵
 */
public record PieceLayout(Map<Coordinate, Piece> pieces) {
    public PieceLayout {
        Objects.requireNonNull(pieces, "pieces cannot be null");
        pieces = Map.copyOf(pieces);
    }
}
