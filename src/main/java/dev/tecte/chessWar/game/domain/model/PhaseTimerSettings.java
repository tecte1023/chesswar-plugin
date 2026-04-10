package dev.tecte.chessWar.game.domain.model;

import lombok.NonNull;

import java.time.Duration;
import java.util.Objects;

/**
 * 특정 게임 단계에서 사용할 타이머의 구성을 정의합니다.
 *
 * @param duration 지속 시간
 * @param visuals  시각적 연출 구성
 */
public record PhaseTimerSettings(Duration duration, TimerVisuals visuals) {
    public PhaseTimerSettings {
        Objects.requireNonNull(duration, "Duration cannot be null");
        Objects.requireNonNull(visuals, "Visuals cannot be null");

        if (duration.isNegative()) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
    }

    /**
     * 타이머 설정을 생성합니다.
     *
     * @param duration 지속 시간
     * @param visuals  시각적 연출 구성
     * @return 타이머 설정
     */
    @NonNull
    public static PhaseTimerSettings of(@NonNull Duration duration, @NonNull TimerVisuals visuals) {
        return new PhaseTimerSettings(duration, visuals);
    }
}
