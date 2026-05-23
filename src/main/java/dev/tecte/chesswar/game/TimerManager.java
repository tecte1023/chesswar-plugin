package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.event.ChessTurnStartedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class TimerManager implements Listener {
    private final ChessWar plugin;
    private final GameManager gameManager;

    @Setter
    private ScoreboardManager scoreboardManager;
    private BukkitTask currentTask;
    private int remainingSeconds;

    @EventHandler
    public void onTurnStart(ChessTurnStartedEvent event) {
        startTurnTimer();
    }

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

    public void accelerateTo(int seconds) {
        if (remainingSeconds > seconds) {
            remainingSeconds = seconds;
        }
    }
}
