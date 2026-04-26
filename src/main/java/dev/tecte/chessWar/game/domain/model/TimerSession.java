package dev.tecte.chessWar.game.domain.model;

import lombok.NonNull;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 타이머 세션의 상태입니다.
 *
 * @param timerSettings    타이머 설정
 * @param phase            게임 단계
 * @param remainingSeconds 남은 초
 */
public record TimerSession(
        PhaseTimerSettings timerSettings,
        GamePhase phase,
        AtomicInteger remainingSeconds
) {
    public TimerSession {
        Objects.requireNonNull(timerSettings, "Settings cannot be null");
        Objects.requireNonNull(phase, "Phase cannot be null");
        Objects.requireNonNull(remainingSeconds, "Remaining seconds cannot be null");

        if (remainingSeconds.get() < 0) {
            throw new IllegalArgumentException("Remaining seconds cannot be negative");
        }
    }

    /**
     * 타이머 세션을 생성합니다.
     *
     * @param timerSettings 타이머 설정
     * @param phase         게임 단계
     * @param remainingTime 남은 시간
     * @return 타이머 세션
     */
    @NonNull
    public static TimerSession of(
            @NonNull PhaseTimerSettings timerSettings,
            @NonNull GamePhase phase,
            @NonNull Duration remainingTime
    ) {
        return new TimerSession(
                timerSettings,
                phase,
                new AtomicInteger((int) remainingTime.toSeconds())
        );
    }

    /**
     * 시간을 1초 감소시킵니다.
     *
     * @return 남은 초
     */
    public int tick() {
        return remainingSeconds.decrementAndGet();
    }

    /**
     * 타이머를 제한 시간으로 단축합니다.
     * 현재 남은 시간이 목표 시간보다 큰 경우에만 단축됩니다.
     *
     * @param reducedSeconds 단축 시 적용될 남은 초
     * @return 단축 성공 여부
     */
    public boolean accelerateTo(int reducedSeconds) {
        int current = remainingSeconds.get();

        if (current > reducedSeconds) {
            remainingSeconds.set(reducedSeconds);

            return true;
        }

        return false;
    }

    /**
     * 현재 남은 시간을 제공합니다.
     *
     * @return 남은 시간
     */
    @NonNull
    public Duration remainingTime() {
        return Duration.ofSeconds(remainingSeconds.get());
    }

    /**
     * 단축 시 적용될 제한 시간을 제공합니다.
     *
     * @return 단축 제한 시간
     */
    @NonNull
    public Duration reducedDuration() {
        return timerSettings.reducedDuration();
    }

    /**
     * 타이머 진행률을 산출합니다.
     *
     * @return 진행률
     */
    public double progress() {
        return (double) remainingSeconds.get() / timerSettings.initialSeconds();
    }

    /**
     * 타이머 제목을 렌더링합니다.
     *
     * @return 제목
     */
    @NonNull
    public Component renderTitle() {
        return timerSettings.renderTitle(phase.displayName(), remainingTime());
    }
}
