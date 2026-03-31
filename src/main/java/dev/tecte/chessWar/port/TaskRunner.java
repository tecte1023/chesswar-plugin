package dev.tecte.chessWar.port;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 태스크 스케줄링 및 실행을 관리합니다.
 */
public interface TaskRunner {
    /**
     * 태스크를 1회 실행합니다.
     *
     * @param action 실행할 태스크
     * @param delay  지연 시간
     * @return 태스크 ID
     */
    int runOnce(@NonNull Runnable action, long delay);

    /**
     * 태스크를 1회 실행합니다.
     *
     * @param action      실행할 태스크
     * @param delay       지연 시간
     * @param errorSender 예외 알림 대상
     * @param context     로그 문맥
     * @return 태스크 ID
     */
    int runOnce(
            @NonNull Runnable action,
            long delay,
            @Nullable CommandSender errorSender,
            @NonNull String context
    );

    /**
     * 태스크를 반복 실행합니다.
     *
     * @param action 실행할 태스크
     * @param delay  초기 지연 시간
     * @param period 반복 주기
     * @return 태스크 ID
     */
    int runRepeating(
            @NonNull Runnable action,
            long delay,
            long period
    );

    /**
     * 태스크를 반복 실행합니다.
     *
     * @param action      실행할 태스크
     * @param delay       초기 지연 시간
     * @param period      반복 주기
     * @param errorSender 예외 알림 대상
     * @param context     로그 문맥
     * @return 태스크 ID
     */
    int runRepeating(
            @NonNull Runnable action,
            long delay,
            long period,
            @Nullable CommandSender errorSender,
            @NonNull String context
    );

    /**
     * 태스크를 반복 실행합니다.
     *
     * @param actionWithTask 실행할 태스크
     * @param delay          초기 지연 시간
     * @param period         반복 주기
     * @return 태스크 ID
     */
    int runRepeating(
            @NonNull Consumer<BukkitRunnable> actionWithTask,
            long delay,
            long period
    );

    /**
     * 태스크를 반복 실행합니다.
     *
     * @param actionWithTask 실행할 태스크
     * @param delay          초기 지연 시간
     * @param period         반복 주기
     * @param errorSender    예외 알림 대상
     * @param context        로그 문맥
     * @return 태스크 ID
     */
    int runRepeating(
            @NonNull Consumer<BukkitRunnable> actionWithTask,
            long delay,
            long period,
            @Nullable CommandSender errorSender,
            @NonNull String context
    );

    /**
     * 진행 중인 태스크를 중단합니다.
     *
     * @param taskId 중단할 태스크 ID
     */
    void cancel(int taskId);

    /**
     * 모든 태스크를 중단하고 자원을 해제합니다.
     */
    void shutdown();
}
