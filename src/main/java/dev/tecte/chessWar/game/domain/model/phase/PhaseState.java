package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

/**
 * 게임 단계별 상태 정보를 정의합니다.
 */
public sealed interface PhaseState permits
        SetupState,
        SelectionState,
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
