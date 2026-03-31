package dev.tecte.chessWar.game.application.port;

import dev.tecte.chessWar.game.application.GameTaskType;
import lombok.NonNull;

/**
 * 게임 도메인 특화 태스크를 관리합니다.
 */
public interface GameTaskManager {
    /**
     * 태스크를 1회 실행합니다.
     *
     * @param type   태스크 타입
     * @param action 실행할 태스크
     * @param delay  지연 시간
     */
    void runOnce(
            @NonNull GameTaskType type,
            @NonNull Runnable action,
            long delay
    );

    /**
     * 태스크를 반복 실행합니다.
     *
     * @param type   태스크 타입
     * @param action 실행할 태스크
     * @param delay  초기 지연 시간
     * @param period 반복 주기
     */
    void runRepeating(
            @NonNull GameTaskType type,
            @NonNull Runnable action,
            long delay,
            long period
    );

    /**
     * 진행 중인 태스크를 중단합니다.
     *
     * @param type 중단할 태스크 타입
     */
    void cancel(@NonNull GameTaskType type);

    /**
     * 모든 게임 태스크를 종료합니다.
     */
    void shutdown();
}
