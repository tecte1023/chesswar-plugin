package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.event.ChessTurnStartedEvent;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.UUID;

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

    private int whiteTeamTime = 600;
    private int blackTeamTime = 600;

    @EventHandler
    public void onTurnStart(ChessTurnStartedEvent event) {
        if (gameManager.phase() != GamePhase.BATTLE) {
            return;
        }

        Team team = gameManager.findParticipant(event.getPlayer().getUniqueId())
                .map(Participant::team)
                .orElse(Team.WHITE);

        int teamTime = (team == Team.WHITE) ? whiteTeamTime : blackTeamTime;
        startTurnTimer(Math.max(teamTime, 30));
    }

    public void startTurnTimer(int seconds) {
        stopTimer();
        remainingSeconds = seconds;
        currentTask = new BukkitRunnable() {
            @Override
            public void run() {
                remainingSeconds--;

                if (gameManager.phase() == GamePhase.BATTLE) {
                    updateSharedTime();
                }

                if (remainingSeconds < 0) {
                    if (gameManager.phase() == GamePhase.BATTLE) {
                        gameManager.nextTurn();
                    } else {
                        gameManager.advancePhase(plugin, plugin.boardManager(), TimerManager.this);
                    }

                    return;
                }

                if (scoreboardManager != null) {
                    scoreboardManager.updateAll();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateSharedTime() {
        Optional<UUID> currentUuid = gameManager.currentTurnPlayer();
        if (currentUuid.isEmpty()) return;

        Team team = gameManager.findParticipant(currentUuid.get())
                .map(Participant::team)
                .orElse(null);

        if (team == Team.WHITE) {
            if (whiteTeamTime > 30) whiteTeamTime = remainingSeconds;
        } else if (team == Team.BLACK) {
            if (blackTeamTime > 30) blackTeamTime = remainingSeconds;
        }
    }

    public void stopTimer() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    public void reset() {
        stopTimer();
        whiteTeamTime = 600;
        blackTeamTime = 600;
        remainingSeconds = 0;
    }

    public void accelerateTo(int seconds) {
        if (remainingSeconds > seconds) {
            remainingSeconds = seconds;
        }
    }
}
