package dev.tecte.chessWar.game.application.port;

import dev.tecte.chessWar.game.application.GameTaskType;
import lombok.NonNull;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

/**
 * 게임 내 비동기 작업 및 스케줄링을 담당하는 포트 인터페이스입니다.
 */
public interface GameTaskScheduler {
    /**
     * 일정 시간 뒤에 로직을 한 번 실행합니다.
     *
     * @param runnable   실행할 로직
     * @param delayTicks 지연 시간 (Tick)
     */
    void scheduleOnce(@NonNull Runnable runnable, long delayTicks);

    /**
     * 일정 간격으로 로직을 반복 실행합니다.
     *
     * @param runnable    실행할 로직
     * @param delayTicks  초기 지연 시간 (Tick)
     * @param periodTicks 반복 주기 (Tick)
     */
    void scheduleRepeat(
            @NonNull Runnable runnable,
            long delayTicks,
            long periodTicks
    );

    /**
     * 일정 간격으로 로직을 반복 실행하며, 실행 로직 내에서 자신의 태스크를 제어할 수 있습니다.
     *
     * @param taskConsumer 실행할 로직 (인자로 현재 실행 중인 {@link BukkitRunnable}을 받음)
     * @param delayTicks   초기 지연 시간 (Tick)
     * @param periodTicks  반복 주기 (Tick)
     */
    void scheduleRepeat(
            @NonNull Consumer<BukkitRunnable> taskConsumer,
            long delayTicks,
            long periodTicks
    );

    /**
     * 특정 타입을 수행하는 작업을 일정 시간 뒤에 실행합니다.
     * 해당 타입으로 이미 진행 중인 작업이 있다면 취소하고 새로 예약합니다.
     *
     * @param type       작업의 타입
     * @param runnable   실행할 로직
     * @param delayTicks 지연 시간 (Tick)
     */
    void scheduleOnce(
            @NonNull GameTaskType type,
            @NonNull Runnable runnable,
            long delayTicks
    );

    /**
     * 특정 타입을 수행하는 작업을 반복 실행합니다.
     * 해당 타입으로 이미 진행 중인 작업이 있다면 취소하고 새로 시작합니다.
     *
     * @param type        작업의 타입
     * @param runnable    실행할 로직
     * @param delayTicks  초기 지연 시간 (Tick)
     * @param periodTicks 반복 주기 (Tick)
     */
    void scheduleRepeat(
            @NonNull GameTaskType type,
            @NonNull Runnable runnable,
            long delayTicks,
            long periodTicks
    );

    /**
     * 특정 역할의 작업을 즉시 중단합니다.
     *
     * @param type 중단할 작업의 역할
     */
    void stop(@NonNull GameTaskType type);

    /**
     * 등록된 모든 작업을 중단하고 레지스트리를 초기화합니다.
     */
    void shutdown();
}
