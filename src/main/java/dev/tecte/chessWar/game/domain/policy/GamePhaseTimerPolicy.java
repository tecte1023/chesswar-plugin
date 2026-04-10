package dev.tecte.chessWar.game.domain.policy;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.PhaseTimerSettings;
import lombok.NonNull;

import java.util.Optional;

/**
 * 게임 단계별 타이머 규칙을 결정합니다.
 */
public interface GamePhaseTimerPolicy {
    /**
     * 특정 단계의 타이머 설정을 찾습니다.
     *
     * @param phase 게임 단계
     * @return 타이머 설정
     */
    @NonNull
    Optional<PhaseTimerSettings> findSettings(@NonNull GamePhase phase);
}
