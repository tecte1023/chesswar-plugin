package dev.tecte.chessWar.game.domain.model.phase;

/**
 * 게임의 각 단계별 상태 데이터를 정의하는 마커 인터페이스입니다.
 * <p>
 * 허용된 구현체만 사용할 수 있도록 제한하여 게임 상태의 타입 안전성을 보장하고
 * 정의되지 않은 임의의 상태 데이터가 사용되는 것을 방지합니다.
 */
public sealed interface PhaseState permits
        SelectionState,
        TurnOrderState,
        BattleState,
        EndedState {
}
