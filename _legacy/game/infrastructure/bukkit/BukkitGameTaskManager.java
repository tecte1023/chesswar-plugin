package dev.tecte.chessWar.game.infrastructure.bukkit;

import dev.tecte.chessWar.game.application.GameTaskType;
import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.port.TaskRunner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bukkit 기반으로 게임 태스크를 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitGameTaskManager implements GameTaskManager {
    private final TaskRunner taskRunner;

    private final Map<GameTaskType, Integer> activeTasks = new ConcurrentHashMap<>();

    @Override
    public void runOnce(@NonNull GameTaskType type, @NonNull Runnable action, long delay) {
        cancel(type);

        int taskId = taskRunner.runOnce(action, delay, null, "Game Task: " + type);

        activeTasks.put(type, taskId);
    }

    @Override
    public void runRepeating(
            @NonNull GameTaskType type,
            @NonNull Runnable action,
            long delay,
            long period
    ) {
        cancel(type);

        int taskId = taskRunner.runRepeating(
                action,
                delay,
                period,
                null,
                "Game Task: " + type
        );

        activeTasks.put(type, taskId);
    }

    @Override
    public void cancel(@NonNull GameTaskType type) {
        Integer taskId = activeTasks.remove(type);

        if (taskId != null) {
            taskRunner.cancel(taskId);
        }
    }

    @Override
    public void shutdown() {
        activeTasks.values().forEach(taskRunner::cancel);
        activeTasks.clear();
    }
}
