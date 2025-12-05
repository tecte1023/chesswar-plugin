package dev.tecte.chessWar.game.application;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 게임 내 스케줄링 및 비동기 태스크를 통합 관리하는 중앙 스케줄러입니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameTaskScheduler {
    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    private final Set<Integer> untypedTasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<GameTaskType, Integer> typedTasks = new ConcurrentHashMap<>();

    /**
     * 일정 시간 뒤에 로직을 한 번 실행합니다.
     *
     * @param runnable   실행할 로직
     * @param delayTicks 지연 시간 (Tick)
     */
    public void scheduleOnce(@NonNull Runnable runnable, long delayTicks) {
        trackTask(scheduler.runTaskLater(plugin, wrapSafeRunnable(runnable), delayTicks));
    }

    /**
     * 일정 간격으로 로직을 반복 실행합니다.
     *
     * @param runnable    실행할 로직
     * @param delayTicks  초기 지연 시간 (Tick)
     * @param periodTicks 반복 주기 (Tick)
     */
    public void scheduleRepeat(@NonNull Runnable runnable, long delayTicks, long periodTicks) {
        trackTask(scheduler.runTaskTimer(plugin, wrapSafeRunnable(runnable), delayTicks, periodTicks));
    }

    /**
     * 일정 간격으로 로직을 반복 실행하며, 실행 로직 내에서 자신의 태스크를 제어할 수 있습니다.
     *
     * @param taskConsumer 실행할 로직 (인자로 현재 실행 중인 {@link BukkitRunnable}을 받음)
     * @param delayTicks   초기 지연 시간 (Tick)
     * @param periodTicks  반복 주기 (Tick)
     */
    public void scheduleRepeat(
            @NonNull Consumer<BukkitRunnable> taskConsumer,
            long delayTicks,
            long periodTicks
    ) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    taskConsumer.accept(this);
                } catch (Exception e) {
                    log.error("Unexpected error in self-cancelling game task", e);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, delayTicks, periodTicks);

        trackTask(task);
    }

    /**
     * 특정 타입을 수행하는 작업을 일정 시간 뒤에 실행합니다.
     * 해당 타입으로 이미 진행 중인 작업이 있다면 취소하고 새로 예약합니다.
     */
    public void scheduleOnce(@NonNull GameTaskType type, @NonNull Runnable runnable, long delayTicks) {
        stop(type);
        trackTypedTask(scheduler.runTaskLater(plugin, wrapSafeRunnable(runnable), delayTicks), type);
    }

    /**
     * 특정 타입을 수행하는 작업을 반복 실행합니다.
     * 해당 타입으로 이미 진행 중인 작업이 있다면 취소하고 새로 시작합니다.
     */
    public void scheduleRepeat(
            @NonNull GameTaskType type,
            @NonNull Runnable runnable,
            long delayTicks,
            long periodTicks
    ) {
        stop(type);
        trackTypedTask(scheduler.runTaskTimer(plugin, wrapSafeRunnable(runnable), delayTicks, periodTicks), type);
    }

    /**
     * 특정 역할의 작업을 즉시 중단합니다.
     *
     * @param type 중단할 작업의 역할
     */
    public void stop(@NonNull GameTaskType type) {
        Integer taskId = typedTasks.remove(type);

        if (taskId != null) {
            cancelTaskInternal(taskId);
        }
    }

    /**
     * 등록된 모든 작업을 중단하고 레지스트리를 초기화합니다.
     */
    public void shutdown() {
        if (!untypedTasks.isEmpty()) {
            untypedTasks.forEach(this::cancelTaskInternal);
            untypedTasks.clear();
        }

        if (!typedTasks.isEmpty()) {
            typedTasks.values().forEach(this::cancelTaskInternal);
            typedTasks.clear();
        }
    }

    private void trackTask(@NonNull BukkitTask task) {
        untypedTasks.add(task.getTaskId());
    }

    private void trackTypedTask(@NonNull BukkitTask task, @NonNull GameTaskType type) {
        typedTasks.put(type, task.getTaskId());
    }

    private void cancelTaskInternal(int taskId) {
        try {
            scheduler.cancelTask(taskId);
        } catch (Exception e) {
            log.warn("Failed to cancel task [ID: {}]", taskId, e);
        }
    }

    @NonNull
    private Runnable wrapSafeRunnable(@NonNull Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("Unexpected error in async game task", e);
            }
        };
    }
}
