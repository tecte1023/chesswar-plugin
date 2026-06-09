package dev.tecte.chesswar.game.component;

/**
 * 타이머의 만료 처리 방식을 정의합니다.
 */
public enum TimerPolicy {
    /**
     * 0초 도달 시 즉시 만료 (타이틀 카운트다운용: 3, 2, 1 땡)
     */
    IMMEDIATE,

    /**
     * 0초를 1초간 충분히 보여준 후 만료 (스코어보드용: 2, 1, 0 땡)
     */
    GRACEFUL
}
