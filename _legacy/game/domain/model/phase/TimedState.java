package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.PhaseTimerSettings;
import lombok.NonNull;

import java.time.Duration;

/**
 * 타이머가 포함된 게임 단계 상태입니다.
 */
public sealed interface TimedState extends PhaseState permits SelectionState {
    /**
     * 현재 단계의 남은 시간을 제공합니다.
     *
     * @return 남은 시간
     */
    @NonNull
    Duration remainingTime();

    /**
     * 현재 단계의 타이머 설정을 제공합니다.
     *
     * @return 타이머 설정
     */
    @NonNull
    PhaseTimerSettings timerSettings();

    /**
     * 남은 시간이 업데이트된 새로운 상태를 제공합니다.
     *
     * @param remainingTime 업데이트할 남은 시간
     * @return 업데이트된 상태
     */
    @NonNull
    TimedState remaining(@NonNull Duration remainingTime);
}
