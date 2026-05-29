package dev.tecte.chesswar.game.component;

/**
 * 게임 각 페이즈별 타이머 설정값 및 밸런스 정책을 담는 객체.
 * 추후 config.yml 연동을 위한 확장 포인트를 제공함.
 */
public record PhaseTimerSettings(
        int barracksSelectionTime,
        int turnOrderSelectionTime,
        int battleTurnTime,
        int readyAccelerateTime
) {
    /**
     * 기본 기획 수치로 초기화된 기본 설정 생성.
     */
    public static PhaseTimerSettings createDefault() {
        return new PhaseTimerSettings(
                300, // 기물 선택 (5분)
                180, // 순서 정하기 (3분)
                30,  // 전투 턴 (30초)
                10   // 모두 준비 완료 시 가속 시간 (10초)
        );
    }
}
