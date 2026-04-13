package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.common.identity.ProjectIdentity;
import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.game.application.port.GameTimerDisplay;
import dev.tecte.chessWar.game.domain.event.GamePhaseExpiredEvent;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.phase.TimedState;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * 게임 단계별 타이머의 생명주기를 관리합니다.
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
     * @param state          타이머 상태
     * @param participantIds 참여자 ID 목록
     */
    public void start(
            @NonNull GamePhase phase,
            @NonNull TimedState state,
            @NonNull Collection<UUID> participantIds
    ) {
        stop();

        currentSession = TimerSession.of(state.timerSettings(), phase, state.remainingTime());
        timerDisplay.show(participantIds, currentSession.renderTitle(), currentSession.progress());
        taskManager.runRepeating(GameTaskType.TIMER, this::tick, 0L, TICKS_PER_SECOND);
    }

    /**
     * 타이머를 중단합니다.
     */
    public void stop() {
        taskManager.cancel(GameTaskType.TIMER);
        timerDisplay.hide();
        currentSession = null;
    }

    /**
     * 타이머 표시를 복구합니다.
     *
     * @param playerId 플레이어 ID
     */
    public void restore(@NonNull UUID playerId) {
        if (isActive()) {
            timerDisplay.show(playerId);
        }
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
     * 남은 시간을 제공합니다.
     *
     * @return 남은 시간
     */
    @NonNull
    public Optional<Duration> remainingTime() {
        return isActive() ? Optional.of(currentSession.remainingTime()) : Optional.empty();
    }

    private void tick() {
        TimerSession session = currentSession;

        if (session == null) {
            return;
        }

        int secondsLeft = session.tick();

        if (secondsLeft <= 0) {
            handleTimeout(session.phase());

            return;
        }

        timerDisplay.update(session.renderTitle(), session.progress());
    }

    private void handleTimeout(@NonNull GamePhase phase) {
        stop();
        eventDispatcher.dispatch(GamePhaseExpiredEvent.of(phase, ProjectIdentity.SYSTEM_ID));
    }
}
