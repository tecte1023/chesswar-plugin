package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

/**
 * 게임의 각 단계별 상태 데이터를 나타내는 인터페이스입니다.
 * <p>
 * 허용된 구현체만 사용하여 단계별 데이터의 무결성을 보장하며,
 * 각 상태는 자신이 속한 게임 단계를 정의합니다.
 */
public sealed interface PhaseState permits
        SelectionState,
        TurnOrderState,
        BattleState,
        EndedState {
    /**
     * 현재 상태의 게임 단계를 반환합니다.
     *
     * @return 게임 단계
     */
    @NonNull
    GamePhase phase();
}
