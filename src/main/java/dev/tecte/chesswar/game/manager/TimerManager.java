package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GameResetEvent;
import dev.tecte.chesswar.game.component.TimerContext;
import dev.tecte.chesswar.game.component.TurnStartedEvent;
import dev.tecte.chesswar.game.state.GameState;
import dev.tecte.chesswar.team.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.logging.Level;

public class TimerManager implements Listener {
    private static final Component OPEN_BRACKET = Component.text("[ ", NamedTextColor.GRAY);

    private static final Component CLOSE_BRACKET = Component.text(" ]", NamedTextColor.GRAY);

    private static final int TEAM_TIME_MULTIPLIER = 10;

    private static final long TICKS_PER_SECOND = 20L;

    private final ChessWar plugin;

    private final TimerContext context = new TimerContext();

    private BukkitTask heartbeatTask;

    public TimerManager(final ChessWar plugin) {
        this.plugin = plugin;

        reset();
        startHeartbeat();
    }

    public void startHeartbeat() {
        if (heartbeatTask != null) {
            return;
        }

        heartbeatTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    tick();
                } catch (final Exception e) {
                    plugin.getLogger().severe("Timer heartbeat error: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 0L, TICKS_PER_SECOND);
    }

    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();

            heartbeatTask = null;
        }
    }

    public void startTimer(final int seconds) {
        context.remainingSeconds(seconds);

        context.running(true);
    }

    public void stopTimer() {
        context.running(false);
    }

    public void reset() {
        context.reset(plugin.gameManager().timerSettings().battleTurnTime() * TEAM_TIME_MULTIPLIER);
    }

    public void tick() {
        if (!context.running()) {
            return;
        }

        context.remainingSeconds(context.remainingSeconds() - 1);

        final int remaining = context.remainingSeconds();

        sendFeedback(remaining);

        final GameState currentState = plugin.gameManager().currentState();

        if (currentState != null) {
            currentState.onTimerTick(plugin.gameManager(), this);
        }

        if (remaining <= 0) {
            context.running(false);

            if (currentState != null) {
                currentState.onTimerExpire(plugin.gameManager());
            }
        }
    }

    public int teamTime(final Team team) {
        return (team == Team.WHITE) ? context.whiteTeamTime() : context.blackTeamTime();
    }

    public void teamTime(final Team team, final int seconds) {
        if (team == Team.WHITE) {
            context.whiteTeamTime(seconds);
        } else {
            context.blackTeamTime(seconds);
        }
    }

    public void accelerateTo(final int seconds) {
        if (context.remainingSeconds() > seconds) {
            context.remainingSeconds(seconds);
        }
    }

    public int remainingSeconds() {
        return context.remainingSeconds();
    }

    public boolean running() {
        return context.running();
    }

    @EventHandler
    public void onGameReset(final GameResetEvent event) {
        reset();
    }

    @EventHandler
    public void onTurnStart(final TurnStartedEvent event) {
        final GameState currentState = plugin.gameManager().currentState();

        if (currentState == null) {
            return;
        }

        try {
            currentState.onTurnStart(plugin.gameManager(), event.getPlayer());
        } catch (final Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delegate TurnStartedEvent to current state", e);
        }
    }

    private void sendFeedback(final int seconds) {
        if (seconds < 0) {
            return;
        }

        final int minutes = seconds / 60;
        final int secondsRemainder = seconds % 60;
        final String timeString = (minutes < 10 ? "0" : "") + minutes + ":" + (secondsRemainder < 10 ? "0" : "") + secondsRemainder;

        final Component feedback = Component.text()
                .append(OPEN_BRACKET)
                .append(Component.text(timeString, NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(CLOSE_BRACKET)
                .build();

        for (final UUID playerId : plugin.gameManager().participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player == null) {
                continue;
            }

            player.sendActionBar(feedback);

            if (seconds <= 5 && seconds > 0) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
            }
        }
    }
}
