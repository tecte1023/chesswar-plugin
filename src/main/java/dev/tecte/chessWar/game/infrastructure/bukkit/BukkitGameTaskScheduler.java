package dev.tecte.chessWar.game.infrastructure.bukkit;

import dev.tecte.chessWar.game.application.GameTaskType;
import dev.tecte.chessWar.game.application.port.GameTaskScheduler;
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
 * 게임 내 스케줄링 및 비동기 태스크를 통합 관리하는 중앙 스케줄러의 Bukkit 구현체입니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitGameTaskScheduler implements GameTaskScheduler {
    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    private final Set<Integer> untypedTasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<GameTaskType, Integer> typedTasks = new ConcurrentHashMap<>();

    @Override
    public void scheduleOnce(@NonNull Runnable runnable, long delayTicks) {
        trackTask(scheduler.runTaskLater(plugin, wrapSafeRunnable(runnable), delayTicks));
    }

    @Override
    public void scheduleRepeat(@NonNull Runnable runnable, long delayTicks, long periodTicks) {
        trackTask(scheduler.runTaskTimer(plugin, wrapSafeRunnable(runnable), delayTicks, periodTicks));
    }

    @Override
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

    @Override
    public void scheduleOnce(@NonNull GameTaskType type, @NonNull Runnable runnable, long delayTicks) {
        stop(type);
        trackTypedTask(scheduler.runTaskLater(plugin, wrapSafeRunnable(runnable), delayTicks), type);
    }

    @Override
    public void scheduleRepeat(
            @NonNull GameTaskType type,
            @NonNull Runnable runnable,
            long delayTicks,
            long periodTicks
    ) {
        stop(type);
        trackTypedTask(scheduler.runTaskTimer(plugin, wrapSafeRunnable(runnable), delayTicks, periodTicks), type);
    }

    @Override
    public void stop(@NonNull GameTaskType type) {
        Integer taskId = typedTasks.remove(type);

        if (taskId != null) {
            cancelTaskInternal(taskId);
        }
    }

    @Override
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
                log.error("Unexpected error in game task", e);
            }
        };
    }
}
