package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
    private final PieceManager pieceManager;

    private BukkitTask heartbeatTask;

    private final TimerContext context = new TimerContext();

    public void startHeartbeat() {
        if (heartbeatTask != null) {
            return;
        }

        heartbeatTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
    }

    @EventHandler
    public void onTurnStart(TurnStartedEvent event) {
        if (gameManager.phase() != GamePhase.BATTLE) {
            return;
        }

        Team team = gameManager.findParticipant(event.getPlayer().getUniqueId())
                .map(Participant::team)
                .orElse(Team.WHITE);

        int teamTime = (team == Team.WHITE) ? context.whiteTeamTime() : context.blackTeamTime();
        startTurnTimer(Math.max(teamTime, 30));
    }

    public void startTurnTimer(int seconds) {
        context.remainingSeconds(seconds);
        context.running(true);
    }

    public void tick() {
        if (!context.running()) {
            return;
        }

        context.remainingSeconds(context.remainingSeconds() - 1);

        if (gameManager.phase() == GamePhase.BATTLE) {
            updateSharedTime();
        }

        if (context.remainingSeconds() < 0) {
            context.running(false);
            if (gameManager.phase() == GamePhase.BATTLE) {
                gameManager.nextTurn(pieceManager);
            } else {
                gameManager.advancePhase(plugin, plugin.boardManager(), pieceManager, this);
            }
        }
    }

    private void updateSharedTime() {
        Optional<UUID> currentUuid = gameManager.currentTurnPlayer();
        if (currentUuid.isEmpty()) return;

        Team team = gameManager.findParticipant(currentUuid.get())
                .map(Participant::team)
                .orElse(null);

        if (team == Team.WHITE) {
            if (context.whiteTeamTime() > 30) context.whiteTeamTime(context.remainingSeconds());
        } else if (team == Team.BLACK) {
            if (context.blackTeamTime() > 30) context.blackTeamTime(context.remainingSeconds());
        }
    }

    public void stopTimer() {
        context.running(false);
    }

    public void reset() {
        context.reset();
    }

    public void accelerateTo(int seconds) {
        if (context.remainingSeconds() > seconds) {
            context.remainingSeconds(seconds);
        }
    }

    public int remainingSeconds() {
        return context.remainingSeconds();
    }
}
