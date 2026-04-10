package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.common.identity.ProjectIdentity;
import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.game.application.port.GameTimerDisplay;
import dev.tecte.chessWar.game.domain.event.GamePhaseExpiredEvent;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.PhaseTimerSettings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
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

    private TimerSession currentSession;

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
        currentSession = TimerSession.of(phase, settings);

        Component initialTitle = settings.visuals().renderTitle(phase.displayName(), settings.duration());

        timerDisplay.show(participantIds, initialTitle, BossBar.MAX_PROGRESS);
        taskManager.runRepeating(GameTaskType.TIMER, this::tick, 0L, TICKS_PER_SECOND);
    }

    /**
     * 타이머를 복구하여 보여줍니다.
     *
     * @param playerId 플레이어 ID
     */
    public void restore(@NonNull UUID playerId) {
        if (!isActive()) {
            return;
        }

        timerDisplay.show(playerId);
    }

    /**
     * 타이머 활성화 여부를 확인합니다.
     *
     * @return 활성화 여부
     */
    public boolean isActive() {
        return currentSession != null;
    }

    /**
     * 타이머를 중단합니다.
     */
    public void stop() {
        taskManager.cancel(GameTaskType.TIMER);
        timerDisplay.hide();
        currentSession = null;
    }

    private void tick() {
        TimerSession session = currentSession;

        if (session == null) {
            return;
        }

        GamePhase phase = session.phase();
        int secondsLeft = session.remainingSeconds().decrementAndGet();

        if (secondsLeft <= 0) {
            handleTimeout(phase);

            return;
        }

        PhaseTimerSettings settings = session.settings();
        double progress = (double) secondsLeft / settings.duration().toSeconds();
        Component title = settings.visuals().renderTitle(phase.displayName(), Duration.ofSeconds(secondsLeft));

        timerDisplay.update(title, progress);
    }

    private void handleTimeout(@NonNull GamePhase phase) {
        stop();
        eventDispatcher.dispatch(GamePhaseExpiredEvent.of(phase, ProjectIdentity.SYSTEM_ID));
    }

    private record TimerSession(
            @NonNull GamePhase phase,
            @NonNull PhaseTimerSettings settings,
            @NonNull AtomicInteger remainingSeconds
    ) {
        private TimerSession {
            Objects.requireNonNull(phase, "Phase cannot be null");
            Objects.requireNonNull(settings, "Settings cannot be null");
            Objects.requireNonNull(remainingSeconds, "Remaining seconds cannot be null");
        }

        static TimerSession of(GamePhase phase, PhaseTimerSettings settings) {
            return new TimerSession(
                    phase,
                    settings,
                    new AtomicInteger((int) settings.duration().toSeconds())
            );
        }
    }
}
