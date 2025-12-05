package dev.tecte.chessWar.game.application;

/**
 * {@code GameTaskScheduler}에서 관리되는 제어 가능한 태스크의 역할을 정의합니다.
 * 이 타입을 사용하여 스케줄링된 태스크는 해당 역할에 대해 유일성이 보장되며,
 * 필요 시 언제든지 역할 키를 통해 중단하거나 교체할 수 있습니다.
 */
public enum GameTaskType {
    /**
     * 화면에 지속적으로 안내 메시지를 띄우는 가이드 태스크.
     * (새로운 가이드가 시작되면 이전 가이드는 자동으로 종료되어야 함)
     */
    GUIDANCE,

    /**
     * 게임 단계 전환을 예약하는 타이머 태스크.
     * (단계 전환이 중복 예약되지 않도록 유일성 보장 필요)
     */
    PHASE_TRANSITION,

    /**
     * 게임 종료 후 데이터를 정리하기 위한 태스크.
     * (정리 작업이 중복 예약되지 않도록 관리)
     */
    GAME_CLEANUP
}
