package dev.tecte.chessWar.game.domain.model;

import lombok.NonNull;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * 게임 단계별 타이머 설정입니다.
 *
 * @param duration 제한 시간
 * @param visuals  시각 효과 설정
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
     * @param duration 제한 시간
     * @param visuals  시각 효과 설정
     * @return 타이머 설정
     */
    @NonNull
    public static PhaseTimerSettings of(@NonNull Duration duration, @NonNull TimerVisuals visuals) {
        return new PhaseTimerSettings(duration, visuals);
    }

    /**
     * 제한 시간을 초 단위로 제공합니다.
     *
     * @return 제한 시간
     */
    public long totalSeconds() {
        return duration.toSeconds();
    }

    /**
     * 타이머 제목을 렌더링합니다.
     *
     * @param phaseName     단계 이름
     * @param remainingTime 남은 시간
     * @return 제목
     */
    @NonNull
    public Component renderTitle(@NonNull String phaseName, @NonNull Duration remainingTime) {
        return visuals.renderTitle(phaseName, remainingTime);
    }
}
