package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class TimerManager {
    private final ChessWar plugin;
    private final GameManager gameManager;

    @Setter
    private ScoreboardManager scoreboardManager;
    private BukkitTask currentTask;
    private int remainingSeconds;

    public void startTurnTimer() {
        stopTimer();
        remainingSeconds = 30;
        currentTask = new BukkitRunnable() {
            @Override
            public void run() {
                remainingSeconds--;

                if (remainingSeconds < 0) {
                    gameManager.nextTurn();
                    startTurnTimer();

                    return;
                }

                if (scoreboardManager != null) {
                    scoreboardManager.updateAll();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopTimer() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }
}
