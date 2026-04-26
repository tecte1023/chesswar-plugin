package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.common.identity.ProjectIdentity;
import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.game.application.port.GameTimerDisplay;
import dev.tecte.chessWar.game.domain.event.GamePhaseExpiredEvent;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.TimerSession;
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
     * @param participantIds 참가자 ID 목록
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
     * @param participantId 참가자 ID
     */
    public void restore(@NonNull UUID participantId) {
        if (isActive()) {
            timerDisplay.show(participantId);
        }
    }

    /**
     * 현재 활성화된 타이머를 설정된 제한 시간으로 단축합니다.
     */
    public void accelerate() {
        if (isActive()) {
            accelerate(currentSession.reducedDuration());
        }
    }

    /**
     * 타이머를 특정 시간으로 단축합니다.
     *
     * @param reducedTime 목표 제한 시간
     */
    public void accelerate(@NonNull Duration reducedTime) {
        int reducedSeconds = (int) reducedTime.toSeconds();

        if (isActive() && currentSession.accelerateTo(reducedSeconds)) {
            timerDisplay.update(currentSession.renderTitle(), currentSession.progress());
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
