package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.model.phase.PhaseState;
import dev.tecte.chessWar.game.domain.model.phase.SelectionState;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 진행 중인 체스 게임의 상태를 나타내는 불변 객체입니다.
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
     * 초기 상태의 게임을 생성합니다.
     *
     * @param board 체스판
     * @return 생성된 게임
     */
    @NonNull
    public static Game create(@NonNull Board board) {
        return new Game(board, new HashMap<>(), SelectionState.empty());
    }

    /**
     * 주어진 구성 요소들로 게임을 생성합니다.
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
     * 현재 게임 단계를 반환합니다.
     *
     * @return 게임 단계
     */
    @NonNull
    public GamePhase phase() {
        return state.phase();
    }

    /**
     * 플레이어의 기물 선택 정보를 반영한 새로운 게임을 반환합니다.
     *
     * @param playerId 선택한 플레이어의 식별자
     * @param pieceId  선택된 기물의 식별자
     * @return 업데이트된 게임
     * @throws GameException 현재 단계가 기물 선택 단계가 아닐 경우
     */
    @NonNull
    public Game selectPiece(@NonNull UUID playerId, @NonNull UUID pieceId) {
        if (phase() != GamePhase.PIECE_SELECTION) {
            throw GameException.phaseMismatch(GamePhase.PIECE_SELECTION, phase());
        }

        return new Game(board, pieces, ((SelectionState) state).withSelection(playerId, pieceId));
    }

    /**
     * 식별자를 사용하여 게임 내 기물을 찾습니다.
     *
     * @param pieceId 찾을 기물 식별자
     * @return 찾은 기물
     */
    @NonNull
    public Optional<Piece> findPiece(@NonNull UUID pieceId) {
        return pieces.values().stream()
                .filter(piece -> piece.id().equals(pieceId))
                .findFirst();
    }

    /**
     * 특정 팀에 소속된 모든 기물을 찾습니다.
     *
     * @param team 찾을 팀
     * @return 찾은 기물
     */
    @NonNull
    public Stream<Piece> findPiecesByTeam(@NonNull TeamColor team) {
        return pieces.values().stream().filter(piece -> piece.isTeam(team));
    }

    /**
     * 게임에 포함된 모든 일반 기물을 반환합니다.
     *
     * @return 일반 기물 목록
     */
    @NonNull
    public List<UnitPiece> unitPieces() {
        return pieces.values().stream()
                .filter(piece -> piece instanceof UnitPiece)
                .map(piece -> (UnitPiece) piece)
                .toList();
    }

    /**
     * 특정 기물이 플레이어에게 선택되었는지 확인합니다.
     * <p>
     * 기물 선택 단계에서만 유효하며, 그 외의 단계에서는 항상 false를 반환합니다.
     *
     * @param pieceId 기물의 식별자
     * @return 선택 여부
     */
    public boolean isPieceSelected(@NonNull UUID pieceId) {
        if (state instanceof SelectionState selectionState) {
            return selectionState.isSelected(pieceId);
        }

        return false;
    }

    /**
     * 기물 목록이 추가된 게임을 반환합니다.
     *
     * @param pieces 추가할 기물 배치
     * @return 업데이트된 게임
     */
    @NonNull
    public Game withPieces(@NonNull Map<Coordinate, ? extends Piece> pieces) {
        Map<Coordinate, Piece> newPieces = new HashMap<>(this.pieces);

        newPieces.putAll(pieces);

        return new Game(board, newPieces, state);
    }
}
