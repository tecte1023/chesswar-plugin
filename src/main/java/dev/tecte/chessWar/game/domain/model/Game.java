package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.model.phase.PhaseState;
import dev.tecte.chessWar.game.domain.model.phase.SelectionState;
import dev.tecte.chessWar.game.domain.model.phase.TurnOrderState;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 진행 중인 체스 게임의 상태를 나타내는 불변 데이터 객체입니다.
 */
public record Game(
        Board board,
        Map<Coordinate, UnitPiece> pieces,
        GamePhase phase,
        PhaseState state
) {
    public Game {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(pieces, "Pieces map cannot be null");
        Objects.requireNonNull(phase, "Game phase cannot be null");
        Objects.requireNonNull(state, "Phase state cannot be null");

        pieces = Map.copyOf(pieces);
    }

    /**
     * 초기 상태의 게임 객체를 생성합니다.
     *
     * @param board 체스판
     * @return 생성된 게임 객체
     */
    @NonNull
    public static Game create(@NonNull Board board) {
        return new Game(board, new HashMap<>(), GamePhase.initial(), SelectionState.empty());
    }

    /**
     * 주어진 구성 요소들로 게임 객체를 생성합니다.
     *
     * @param board  체스판
     * @param pieces 기물 배치 정보
     * @param phase  현재 게임 단계
     * @param state  단계별 상태 데이터
     * @return 생성된 게임 객체
     */
    @NonNull
    public static Game of(
            @NonNull Board board,
            @NonNull Map<Coordinate, UnitPiece> pieces,
            @NonNull GamePhase phase,
            @NonNull PhaseState state
    ) {
        return new Game(board, pieces, phase, state);
    }

    /**
     * 턴 순서 선택 단계로 전환된 게임 객체를 반환합니다.
     *
     * @return 전환된 게임 객체
     * @throws GameException 현재 단계가 기물 선택 단계가 아닐 경우
     */
    @NonNull
    public Game startTurnSelection() {
        if (phase != GamePhase.PIECE_SELECTION) {
            throw GameException.phaseMismatch(GamePhase.PIECE_SELECTION, phase);
        }

        return new Game(board, pieces, GamePhase.TURN_ORDER_SELECTION, new TurnOrderState());
    }

    /**
     * 엔티티 ID를 사용하여 게임 내 기물을 찾습니다.
     *
     * @param entityId 찾을 엔티티 UUID
     * @return 검색된 기물 정보
     */
    @NonNull
    public Optional<UnitPiece> findPiece(@NonNull UUID entityId) {
        return pieces.values().stream()
                .filter(piece -> piece.entityId().equals(entityId))
                .findFirst();
    }

    /**
     * 기물 정보가 갱신된 게임 객체를 반환합니다.
     *
     * @param newPiece 갱신할 기물 객체
     * @return 갱신된 게임 객체
     * @throws GameException 해당 기물이 존재하지 않을 경우
     */
    @NonNull
    public Game updatePiece(@NonNull UnitPiece newPiece) {
        Coordinate coordinate = pieces.entrySet().stream()
                .filter(entry -> entry.getValue().entityId().equals(newPiece.entityId()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(GameException::pieceNotFound);

        return withPiece(coordinate, newPiece);
    }

    /**
     * 기물 목록이 추가된 게임 객체를 반환합니다.
     *
     * @param additionalPieces 추가할 기물 배치 정보
     * @return 업데이트된 게임 객체
     */
    @NonNull
    public Game withPieces(@NonNull Map<Coordinate, UnitPiece> additionalPieces) {
        Map<Coordinate, UnitPiece> newPieces = new HashMap<>(pieces);

        newPieces.putAll(additionalPieces);

        return new Game(board, newPieces, phase, state);
    }

    /**
     * 특정 위치에 기물이 배치된 게임 객체를 반환합니다.
     *
     * @param coordinate 기물 배치 좌표
     * @param piece      배치할 기물
     * @return 업데이트된 게임 객체
     */
    @NonNull
    public Game withPiece(@NonNull Coordinate coordinate, @NonNull UnitPiece piece) {
        Map<Coordinate, UnitPiece> newPieces = new HashMap<>(pieces);

        newPieces.put(coordinate, piece);

        return new Game(board, newPieces, phase, state);
    }
}
