package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import lombok.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 진행 중인 체스 게임의 상태를 나타내는 불변 데이터 객체입니다.
 * 게임이 진행되는 보드와 현재 보드 위에 배치된 기물들의 상태를 포함합니다.
 */
public record Game(Board board, Map<Coordinate, Piece> pieces) {
    public Game {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(pieces, "Pieces map cannot be null");

        pieces = Map.copyOf(pieces);
    }

    /**
     * 지정된 좌표의 기물 정보를 조회합니다.
     *
     * @param coordinate 조회할 좌표
     * @return 해당 좌표에 기물이 있으면 {@link Optional}를, 없으면 빈 {@link Optional}을 반환
     */
    @NonNull
    public Optional<Piece> getPieceAt(@NonNull Coordinate coordinate) {
        return Optional.ofNullable(pieces.get(coordinate));
    }
}
