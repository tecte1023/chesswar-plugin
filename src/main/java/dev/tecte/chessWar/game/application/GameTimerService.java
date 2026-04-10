package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.common.identity.ProjectIdentity;
import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.game.application.port.GameTimerDisplay;
import dev.tecte.chessWar.game.domain.event.GamePhaseExpiredEvent;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.PhaseTimerSettings;
import dev.tecte.chessWar.game.domain.model.TimerVisuals;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 게임 타이머를 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameTimerService {
    private static final long TICKS_PER_SECOND = 20L;

    private final GameTaskManager taskManager;
    private final GameTimerDisplay timerDisplay;
    private final DomainEventDispatcher eventDispatcher;

    private final AtomicInteger remainingSeconds = new AtomicInteger(0);
    private GamePhase currentPhase;

    /**
     * 타이머를 시작합니다.
     *
     * @param phase          게임 단계
     * @param settings       타이머 설정
     * @param participantIds 참여자 ID 목록
     */
    public void start(
            @NonNull GamePhase phase,
            @NonNull PhaseTimerSettings settings,
            @NonNull Collection<UUID> participantIds
    ) {
        stop();
        currentPhase = phase;

        Duration duration = settings.duration();
        int total = (int) duration.toSeconds();
        TimerVisuals visuals = settings.visuals();
        Component initialTitle = visuals.renderTitle(phase.displayName(), duration);

        remainingSeconds.set(total);
        timerDisplay.show(participantIds, initialTitle, BossBar.MAX_PROGRESS);
        taskManager.runRepeating(GameTaskType.TIMER, () -> tick(total, visuals), 0L, TICKS_PER_SECOND);
    }

    /**
     * 타이머를 중단합니다.
     */
    public void stop() {
        taskManager.cancel(GameTaskType.TIMER);
        timerDisplay.hide();
    }

    private void tick(int total, @NonNull TimerVisuals visuals) {
        int left = remainingSeconds.decrementAndGet();

        if (left <= 0) {
            handleTimeout();

            return;
        }

        double progress = (double) left / total;
        Component title = visuals.renderTitle(currentPhase.displayName(), Duration.ofSeconds(left));

        timerDisplay.update(title, progress);
    }

    private void handleTimeout() {
        stop();
        eventDispatcher.dispatch(GamePhaseExpiredEvent.of(currentPhase, ProjectIdentity.SYSTEM_ID));
    }
}
