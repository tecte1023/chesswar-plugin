package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.model.phase.PhaseState;
import dev.tecte.chessWar.game.domain.model.phase.SelectionState;
import dev.tecte.chessWar.game.domain.model.phase.SetupState;
import dev.tecte.chessWar.game.domain.model.phase.TimedState;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import lombok.NonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 게임의 진행 상태와 단계 전이를 관리하는 애그리거트 루트입니다.
 *
 * @param board  체스판
 * @param pieces 기물 배치
 * @param state  단계별 상태
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
     * @return 게임
     */
    @NonNull
    public static Game create(@NonNull Board board) {
        return new Game(board, new HashMap<>(), SetupState.initial());
    }

    /**
     * 게임을 생성합니다.
     *
     * @param board  체스판
     * @param pieces 기물 배치
     * @param state  단계별 상태
     * @return 게임
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
     * 기물 선택 단계를 시작합니다.
     *
     * @param initialPlacements 초기 기물 배치
     * @param timerSettings     타이머 설정
     * @return 업데이트된 게임
     * @throws GameException 준비 단계가 아닐 경우
     */
    @NonNull
    public Game startSelection(
            @NonNull Map<Coordinate, UnitPiece> initialPlacements,
            @NonNull PhaseTimerSettings timerSettings
    ) {
        if (!(state instanceof SetupState)) {
            throw GameException.phaseMismatch(GamePhase.SETUP, phase());
        }

        Map<Coordinate, Piece> newPieces = new HashMap<>(pieces);

        newPieces.putAll(initialPlacements);

        return new Game(board, newPieces, SelectionState.initial(timerSettings));
    }

    /**
     * 플레이어의 기물 선택을 반영합니다.
     *
     * @param playerId 플레이어 ID
     * @param pieceId  기물 ID
     * @return 업데이트된 게임
     * @throws GameException 단계 불일치, 기물 미발견, 선택 불가 또는 이미 선택된 경우
     */
    @NonNull
    public Game selectPiece(@NonNull UUID playerId, @NonNull UUID pieceId) {
        if (!(state instanceof SelectionState selection)) {
            throw GameException.phaseMismatch(GamePhase.PIECE_SELECTION, phase());
        }

        Piece piece = findPiece(pieceId).orElseThrow(GameException::pieceNotFound);

        if (!piece.isSelectable()) {
            throw GameException.unselectablePieceType(piece.spec().type());
        }

        if (isAlreadySelected(pieceId)) {
            throw GameException.pieceAlreadySelected();
        }

        return atState(selection.select(playerId, pieceId));
    }

    /**
     * 남은 시간이 업데이트된 게임을 제공합니다.
     *
     * @param remainingTime 남은 시간
     * @return 업데이트된 게임
     */
    @NonNull
    public Game remaining(@NonNull Duration remainingTime) {
        return (state instanceof TimedState timedState)
                ? atState(timedState.remaining(remainingTime))
                : this;
    }

    /**
     * 현재 단계를 제공합니다.
     *
     * @return 현재 단계
     */
    @NonNull
    public GamePhase phase() {
        return state.phase();
    }

    /**
     * 타이머 상태를 제공합니다.
     *
     * @return 타이머 상태
     */
    @NonNull
    public Optional<TimedState> timedState() {
        return (state instanceof TimedState timedState) ? Optional.of(timedState) : Optional.empty();
    }

    /**
     * 현재 기물 선택 단계인지 확인합니다.
     *
     * @return 기물 선택 단계 여부
     */
    public boolean isInSelectionPhase() {
        return state instanceof SelectionState;
    }

    /**
     * ID로 기물을 검색합니다.
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
     * 기물이 이미 선택되었는지 확인합니다.
     *
     * @param pieceId 기물 ID
     * @return 기물 선택 여부
     */
    public boolean isAlreadySelected(@NonNull UUID pieceId) {
        return (state instanceof SelectionState selection) && selection.isSelected(pieceId);
    }

    /**
     * 플레이어의 기물 선택 완료 여부를 확인합니다.
     *
     * @param playerId 플레이어 ID
     * @return 선택 완료 여부
     */
    public boolean hasSelectedPiece(@NonNull UUID playerId) {
        return (state instanceof SelectionState selection) && selection.hasSelectionFor(playerId);
    }

    /**
     * 일반 기물 목록을 산출합니다.
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
     * 일반 기물 배치 현황을 산출합니다.
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

    @NonNull
    private Game atState(@NonNull PhaseState nextState) {
        return new Game(board, pieces, nextState);
    }
}
