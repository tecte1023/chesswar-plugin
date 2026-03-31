package dev.tecte.chessWar.infrastructure.bukkit;

import dev.tecte.chessWar.infrastructure.exception.TaskSchedulingException;
import dev.tecte.chessWar.port.TaskRunner;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Bukkit 스케줄러 기반으로 태스크를 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitTaskRunner implements TaskRunner {
    private final ExceptionDispatcher exceptionDispatcher;
    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    private final Set<Integer> activeTasks = ConcurrentHashMap.newKeySet();

    @Override
    public int runOnce(@NonNull Runnable action, long delay) {
        return runOnce(action, delay, null, "Untyped Once Task");
    }

    @Override
    public int runOnce(
            @NonNull Runnable action,
            long delay,
            @Nullable CommandSender errorSender,
            @NonNull String context
    ) {
        BukkitTask task = scheduler.runTaskLater(
                plugin,
                wrapSafeRunnable(action, errorSender, context),
                delay
        );

        return trackTask(task);
    }

    @Override
    public int runRepeating(@NonNull Runnable action, long delay, long period) {
        return runRepeating(action, delay, period, null, "Untyped Repeat Task");
    }

    @Override
    public int runRepeating(
            @NonNull Runnable action,
            long delay,
            long period,
            @Nullable CommandSender errorSender,
            @NonNull String context
    ) {
        BukkitTask task = scheduler.runTaskTimer(
                plugin,
                wrapSafeRunnable(action, errorSender, context),
                delay,
                period
        );

        return trackTask(task);
    }

    @Override
    public int runRepeating(
            @NonNull Consumer<BukkitRunnable> actionWithTask,
            long delay,
            long period
    ) {
        return runRepeating(actionWithTask, delay, period, null, "Untyped Consumer Task");
    }

    @Override
    public int runRepeating(
            @NonNull Consumer<BukkitRunnable> actionWithTask,
            long delay,
            long period,
            @Nullable CommandSender errorSender,
            @NonNull String context
    ) {
        BukkitTask task = createSafeRepeatTask(actionWithTask, errorSender, context)
                .runTaskTimer(plugin, delay, period);

        return trackTask(task);
    }

    @Override
    public void cancel(int taskId) {
        cancelTaskInternal(taskId);
        activeTasks.remove(taskId);
    }

    @Override
    public void shutdown() {
        activeTasks.forEach(taskId -> {
            try {
                cancelTaskInternal(taskId);
            } catch (Exception e) {
                exceptionDispatcher.dispatch(e, null, "Task Runner Shutdown Loop");
            }
        });
        activeTasks.clear();
    }

    @NonNull
    private BukkitRunnable createSafeRepeatTask(
            @NonNull Consumer<BukkitRunnable> actionWithTask,
            @Nullable CommandSender errorSender,
            @NonNull String context
    ) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    actionWithTask.accept(this);
                } catch (Exception e) {
                    exceptionDispatcher.dispatch(e, errorSender, context);
                    this.cancel();
                }
            }
        };
    }

    @NonNull
    private Runnable wrapSafeRunnable(
            @NonNull Runnable action,
            @Nullable CommandSender sender,
            @NonNull String context
    ) {
        return () -> {
            try {
                action.run();
            } catch (Exception e) {
                exceptionDispatcher.dispatch(e, sender, context);
            }
        };
    }

    private int trackTask(@NonNull BukkitTask task) {
        int taskId = task.getTaskId();

        activeTasks.add(taskId);

        return taskId;
    }

    private void cancelTaskInternal(int taskId) {
        try {
            scheduler.cancelTask(taskId);
        } catch (Exception e) {
            throw TaskSchedulingException.cancelFailed(taskId, e);
        }
    }
}
