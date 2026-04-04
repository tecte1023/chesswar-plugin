package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.model.phase.PhaseState;
import dev.tecte.chessWar.game.domain.model.phase.SelectionState;
import dev.tecte.chessWar.game.domain.model.phase.SetupState;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 게임 상태와 페이즈 전이를 제어하는 애그리거트 루트입니다.
 *
 * @param board  게임이 진행되는 체스판
 * @param pieces 체스판의 기물 배치 현황
 * @param state  현재 단계별 상태 데이터
 */
public record Game(
        Board board,
        Map<Coordinate, Piece> pieces,
        PhaseState state
) {
    public Game {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(pieces, "Pieces map cannot be null");
        Objects.requireNonNull(state, "Phase state cannot be null");

        pieces = Map.copyOf(pieces);
    }

    /**
     * 초기 준비 상태의 게임을 생성합니다.
     *
     * @param board 체스판
     * @return 초기화된 게임
     */
    @NonNull
    public static Game create(@NonNull Board board) {
        return new Game(board, new HashMap<>(), SetupState.empty());
    }

    /**
     * 주어진 상태로 게임을 생성합니다.
     *
     * @param board  체스판
     * @param pieces 기물 배치
     * @param state  단계별 상태 데이터
     * @return 생성된 게임
     */
    @NonNull
    public static Game of(
            @NonNull Board board,
            @NonNull Map<Coordinate, Piece> pieces,
            @NonNull PhaseState state
    ) {
        return new Game(board, pieces, state);
    }

    /**
     * 기물 선택 단계로 전이합니다.
     *
     * @param spawnedPieces 배치된 기물 정보
     * @return 업데이트된 게임
     * @throws GameException 준비 단계가 아닐 경우
     */
    @NonNull
    public Game startSelection(@NonNull Map<Coordinate, ? extends Piece> spawnedPieces) {
        if (phase() != GamePhase.SETUP) {
            throw GameException.phaseMismatch(GamePhase.SETUP, phase());
        }

        Map<Coordinate, Piece> newPieces = new HashMap<>(pieces);

        newPieces.putAll(spawnedPieces);

        return new Game(board, newPieces, SelectionState.empty());
    }

    /**
     * 플레이어의 기물 선택 결과를 반영합니다.
     *
     * @param playerId 플레이어 ID
     * @param pieceId  기물 ID
     * @return 업데이트된 게임
     * @throws GameException 단계 불일치, 기물 미발견 또는 중복 선택 시
     */
    @NonNull
    public Game selectPiece(@NonNull UUID playerId, @NonNull UUID pieceId) {
        if (phase() != GamePhase.PIECE_SELECTION) {
            throw GameException.phaseMismatch(GamePhase.PIECE_SELECTION, phase());
        }

        Piece piece = findPiece(pieceId).orElseThrow(GameException::pieceNotFound);

        if (!piece.isSelectable()) {
            throw GameException.unselectablePieceType(piece.spec().type());
        }

        if (isAlreadySelected(pieceId)) {
            throw GameException.pieceAlreadySelected();
        }

        return new Game(board, pieces, ((SelectionState) state).withSelection(playerId, pieceId));
    }

    /**
     * 현재 게임 단계를 제공합니다.
     *
     * @return 현재 단계
     */
    @NonNull
    public GamePhase phase() {
        return state.phase();
    }

    /**
     * 중도 참여 가능 여부를 확인합니다.
     *
     * @return 참여 가능 여부
     */
    public boolean isJoinable() {
        return isInSelectionPhase();
    }

    /**
     * 현재 기물 선택 단계인지 확인합니다.
     *
     * @return 기물 선택 단계 여부
     */
    public boolean isInSelectionPhase() {
        return phase() == GamePhase.PIECE_SELECTION;
    }

    /**
     * ID 기반으로 체스판의 기물을 검색합니다.
     *
     * @param pieceId 기물 ID
     * @return 찾은 기물
     */
    @NonNull
    public Optional<Piece> findPiece(@NonNull UUID pieceId) {
        return pieces.values().stream()
                .filter(piece -> piece.id().equals(pieceId))
                .findFirst();
    }

    /**
     * 기물의 선택 여부를 확인합니다.
     *
     * @param pieceId 기물 ID
     * @return 기물 선택 여부
     */
    public boolean isAlreadySelected(@NonNull UUID pieceId) {
        if (state instanceof SelectionState selectionState) {
            return selectionState.isSelected(pieceId);
        }

        return false;
    }

    /**
     * 참여자가 기물을 선택했는지 확인합니다.
     *
     * @param playerId 플레이어 ID
     * @return 기물 선택 완료 여부
     */
    public boolean hasSelectedPiece(@NonNull UUID playerId) {
        if (state instanceof SelectionState selectionState) {
            return selectionState.hasSelectionFor(playerId);
        }

        return false;
    }

    /**
     * 체스판에 배치된 모든 일반 기물 목록을 산출합니다.
     *
     * @return 일반 기물 목록
     */
    @NonNull
    public List<UnitPiece> units() {
        return pieces.values().stream()
                .filter(piece -> piece instanceof UnitPiece)
                .map(piece -> (UnitPiece) piece)
                .toList();
    }

    /**
     * 좌표 기반의 일반 기물 배치 현황을 산출합니다.
     *
     * @return 기물 배치 현황
     */
    @NonNull
    public Map<Coordinate, UnitPiece> unitPlacements() {
        return pieces.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof UnitPiece)
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> (UnitPiece) entry.getValue())
                );
    }
}
