package dev.tecte.chessWar.game.infrastructure.policy;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.PhaseTimerSettings;
import dev.tecte.chessWar.game.domain.model.TimerVisuals;
import dev.tecte.chessWar.game.domain.policy.GamePhaseTimerPolicy;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * 게임 단계별 기본 타이머 규칙을 정의합니다.
 */
@Singleton
public class DefaultGamePhaseTimerPolicy implements GamePhaseTimerPolicy {
    private static final Map<GamePhase, PhaseTimerSettings> SETTINGS = Map.of(
            GamePhase.PIECE_SELECTION, PhaseTimerSettings.of(Duration.ofMinutes(5), new TimerVisuals()),
            GamePhase.TURN_ORDER_SELECTION, PhaseTimerSettings.of(Duration.ofMinutes(3), new TimerVisuals())
    );

    @NonNull
    @Override
    public Optional<PhaseTimerSettings> findSettings(@NonNull GamePhase phase) {
        return Optional.ofNullable(SETTINGS.get(phase));
    }
}
