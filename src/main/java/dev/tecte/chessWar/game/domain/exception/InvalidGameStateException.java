package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Loggable;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

/**
 * 게임의 상태가 특정 작업을 수행하기에 적합하지 않을 때 발생하는 예외입니다.
 * 이 예외는 개발자가 인지해야 할 로직상의 버그를 나타내므로, 로그에는 기록되지만 사용자에게 직접적인 내용은 전달되지 않습니다.
 */
public class InvalidGameStateException extends BusinessException implements Loggable {
    private InvalidGameStateException(@NonNull String message) {
        super(message);
    }

    /**
     * '턴 순서 선택' 단계로의 전환이 부적절할 때 사용합니다.
     *
     * @param currentPhase 현재 게임 단계
     * @return 새로운 예외 객체
     */
    @NonNull
    public static InvalidGameStateException forTurnOrderSelection(@NonNull GamePhase currentPhase) {
        return new InvalidGameStateException(
                String.format("Cannot transition to '%s'. Required phase: '%s', but current phase is '%s'.",
                        GamePhase.TURN_ORDER_SELECTION, GamePhase.PIECE_SELECTION, currentPhase)
        );
    }

    /**
     * '전투' 단계로의 전환이 부적절할 때 사용합니다.
     *
     * @param currentPhase 현재 게임 단계
     * @return 새로운 예외 객체
     */
    @NonNull
    public static InvalidGameStateException forBattlePhase(@NonNull GamePhase currentPhase) {
        return new InvalidGameStateException(
                String.format("Cannot transition to '%s'. Required phase: '%s', but current phase is '%s'.",
                        GamePhase.BATTLE, GamePhase.TURN_ORDER_SELECTION, currentPhase)
        );
    }

    /**
     * '종료' 단계로의 전환이 부적절할 때 사용합니다.
     *
     * @param currentPhase 현재 게임 단계
     * @return 새로운 예외 객체
     */
    @NonNull
    public static InvalidGameStateException forEnding(@NonNull GamePhase currentPhase) {
        return new InvalidGameStateException(
                String.format("Cannot transition to '%s'. Required phase: '%s', but current phase is '%s'.",
                        GamePhase.ENDED, GamePhase.BATTLE, currentPhase)
        );
    }

    /**
     * 다음 턴으로의 전환 시, 단계가 잘못되었을 때 사용합니다.
     *
     * @param currentPhase 현재 게임 단계
     * @return 새로운 예외 객체
     */
    @NonNull
    public static InvalidGameStateException invalidPhaseForNextTurn(@NonNull GamePhase currentPhase) {
        return new InvalidGameStateException(
                String.format("Cannot proceed to next turn. Required phase: '%s', but current phase is '%s'.",
                        GamePhase.BATTLE, currentPhase)
        );
    }

    /**
     * 다음 턴으로의 전환 시, 현재 턴 정보가 null일 때 사용합니다.
     *
     * @return 새로운 예외 객체
     */
    @NonNull
    public static InvalidGameStateException nullTurnForNextTurn() {
        return new InvalidGameStateException("Cannot proceed to next turn because current turn is not set.");
    }
}
