package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.exception.InvalidGameStateException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 진행 중인 체스 게임의 상태를 나타내는 불변 데이터 객체입니다.
 * 게임이 진행되는 보드, 현재 보드 위에 배치된 기물들의 상태, 그리고 게임의 현재 진행 단계를 포함합니다.
 */
public record Game(
        Board board,
        Map<Coordinate, Piece> pieces,
        GamePhase phase,
        TeamColor currentTurn
) {
    public Game {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(pieces, "Pieces map cannot be null");
        Objects.requireNonNull(phase, "Game phase cannot be null");

        pieces = Map.copyOf(pieces);
    }

    /**
     * 새로운 게임을 생성하기 위한 정적 팩토리 메서드입니다.
     *
     * @param board 게임 보드
     * @return 초기 상태로 설정된 새로운 {@link Game} 객체
     */
    @NonNull
    public static Game create(@NonNull Board board) {
        return new Game(board, new HashMap<>(), GamePhase.initial(), null);
    }

    /**
     * 주어진 모든 구성 요소들로 {@link Game} 객체를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param board       게임 보드
     * @param pieces      기물 배치 맵
     * @param phase       현재 게임 단계
     * @param currentTurn 현재 턴을 진행 중인 팀 색상
     * @return 주어진 구성 요소들로 생성된 {@link Game} 객체
     */
    @NonNull
    public static Game of(
            @NonNull Board board,
            @NonNull Map<Coordinate, Piece> pieces,
            @NonNull GamePhase phase,
            @Nullable TeamColor currentTurn
    ) {
        return new Game(board, pieces, phase, currentTurn);
    }

    /**
     * 게임을 '턴 순서 선택' 단계로 전환합니다.
     *
     * @return 상태가 변경된 새로운 {@link Game} 객체
     * @throws InvalidGameStateException 현재 단계가 {@link GamePhase#CLASS_SELECTION} 아닐 경우
     */
    @NonNull
    public Game startTurnSelection() {
        if (phase != GamePhase.CLASS_SELECTION) {
            throw InvalidGameStateException.forTurnOrderSelection(phase);
        }

        return new Game(board, pieces, GamePhase.TURN_ORDER_SELECTION, null);
    }

    /**
     * 게임을 '전투 중' 단계로 전환하고 시작 팀을 설정합니다.
     *
     * @param startingTeam 시작 팀
     * @return 상태가 변경된 새로운 {@link Game} 객체
     * @throws InvalidGameStateException 현재 단계가 {@link GamePhase#TURN_ORDER_SELECTION}이 아닐 경우
     */
    @NonNull
    public Game startBattle(@NonNull TeamColor startingTeam) {
        if (phase != GamePhase.TURN_ORDER_SELECTION) {
            throw InvalidGameStateException.forBattlePhase(phase);
        }

        return new Game(board, pieces, GamePhase.BATTLE, startingTeam);
    }

    /**
     * 게임을 '종료' 단계로 전환합니다.
     *
     * @return 상태가 변경된 새로운 {@link Game} 객체
     * @throws InvalidGameStateException 현재 단계가 {@link GamePhase#BATTLE}이 아닐 경우
     */
    @NonNull
    public Game end() {
        if (phase != GamePhase.BATTLE) {
            throw InvalidGameStateException.forEnding(phase);
        }

        return new Game(board, pieces, GamePhase.ENDED, null);
    }

    /**
     * 게임을 비정상적으로 '종료' 단계로 전환합니다.
     * 이 메서드는 현재 단계와 상관없이 게임을 종료시킬 수 있습니다.
     *
     * @return 상태가 변경된 새로운 {@link Game} 객체
     */
    @NonNull
    public Game abort() {
        return new Game(board, pieces, GamePhase.ENDED, null);
    }

    /**
     * 다음 턴으로 전환합니다.
     *
     * @return 상태가 변경된 새로운 {@link Game} 객체
     * @throws InvalidGameStateException 게임이 {@link GamePhase#BATTLE} 상태가 아닐 경우
     */
    @NonNull
    public Game switchTurn() {
        if (phase != GamePhase.BATTLE) {
            throw InvalidGameStateException.invalidPhaseForNextTurn(phase);
        } else if (currentTurn == null) {
            throw InvalidGameStateException.nullTurnForNextTurn();
        }

        return new Game(board, pieces, phase, currentTurn.opposite());
    }

    /**
     * 현재 활성화된 턴의 팀 색상을 {@link Optional}로 반환합니다.
     * 턴은 BATTLE 단계에서만 정의되므로, 그 외의 경우에는 비어있는 {@link Optional}이 반환됩니다.
     *
     * @return 현재 턴의 팀 색상을 담은 Optional, 또는 턴이 활성화되지 않았을 경우 빈 Optional
     */
    @NonNull
    public Optional<TeamColor> activeTurn() {
        return Optional.ofNullable(currentTurn);
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

    /**
     * 주어진 기물들을 기존 기물 맵에 추가하고 새로운 Game 인스턴스를 반환합니다.
     *
     * @param additionalPieces 추가할 기물들의 맵
     * @return 기물 맵이 업데이트된 새로운 {@link Game} 인스턴스
     */
    @NonNull
    public Game withPieces(@NonNull Map<Coordinate, Piece> additionalPieces) {
        Map<Coordinate, Piece> newPieces = new HashMap<>(pieces);

        newPieces.putAll(additionalPieces);

        return new Game(board, newPieces, phase, currentTurn);
    }

    /**
     * 엔티티 ID를 사용하여 게임에 포함된 기물을 찾습니다.
     * <p>
     * {@link #pieces()} 맵을 순회하며 해당 ID를 가진 기물을 검색합니다.
     *
     * @param entityId 찾을 엔티티의 UUID
     * @return 해당하는 기물이 있으면 {@link Optional}로 반환, 없으면 빈 {@link Optional}
     */
    @NonNull
    public Optional<Piece> findPiece(@NonNull UUID entityId) {
        return pieces.values().stream()
                .filter(piece -> piece.entityId().equals(entityId))
                .findFirst();
    }
}
