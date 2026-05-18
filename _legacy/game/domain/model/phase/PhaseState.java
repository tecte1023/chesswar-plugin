package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

/**
 * 게임의 각 단계별 진행 상태를 나타냅니다.
 */
public sealed interface PhaseState permits
        SetupState,
        TimedState,
        TurnOrderState,
        BattleState,
        EndedState {
    /**
     * 게임 단계를 제공합니다.
     *
     * @return 게임 단계
     */
    @NonNull
    GamePhase phase();
}
