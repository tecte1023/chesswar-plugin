package dev.tecte.chessWar.game.domain.model;

import lombok.NonNull;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * 게임 단계별 타이머 설정입니다.
 *
 * @param initialDuration 초기 제한 시간
 * @param reducedDuration 단축 시 적용될 제한 시간
 * @param visuals         시각 효과 설정
 */
public record PhaseTimerSettings(
        Duration initialDuration,
        Duration reducedDuration,
        TimerVisuals visuals
) {
    public PhaseTimerSettings {
        Objects.requireNonNull(initialDuration, "Initial duration cannot be null");
        Objects.requireNonNull(reducedDuration, "Reduced duration cannot be null");
        Objects.requireNonNull(visuals, "Visuals cannot be null");

        if (initialDuration.isNegative()) {
            throw new IllegalArgumentException("Initial duration cannot be negative");
        }

        if (reducedDuration.isNegative()) {
            throw new IllegalArgumentException("Reduced duration cannot be negative");
        }
    }

    /**
     * 타이머 설정을 생성합니다.
     *
     * @param initialDuration 초기 제한 시간
     * @param reducedDuration 단축 시 적용될 제한 시간
     * @param visuals         시각 효과 설정
     * @return 타이머 설정
     */
    @NonNull
    public static PhaseTimerSettings of(
            @NonNull Duration initialDuration,
            @NonNull Duration reducedDuration,
            @NonNull TimerVisuals visuals
    ) {
        return new PhaseTimerSettings(initialDuration, reducedDuration, visuals);
    }

    /**
     * 초기 제한 시간을 초 단위로 제공합니다.
     *
     * @return 초기 제한 시간
     */
    public long initialSeconds() {
        return initialDuration.toSeconds();
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
