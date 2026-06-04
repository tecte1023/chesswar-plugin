package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor
public class TimerManager {
    private static final Component OPEN_BRACKET = Component.text("[ ", NamedTextColor.GRAY);
    private static final Component CLOSE_BRACKET = Component.text(" ]", NamedTextColor.GRAY);
    private static final int MAX_CACHE_SECONDS = 3600;
    private static final Component[] FEEDBACK_CACHE = new Component[MAX_CACHE_SECONDS + 1];

    static {
        for (int i = 0; i <= MAX_CACHE_SECONDS; i++) {
            final int minutes = i / 60;
            final int seconds = i % 60;
            final String timeString = String.format("%02d:%02d", minutes, seconds);

            FEEDBACK_CACHE[i] = Component.text()
                    .append(OPEN_BRACKET)
                    .append(Component.text(timeString, NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(CLOSE_BRACKET)
                    .build();
        }
    }

    private final GameContext context;

    public void tick() {
        if (!context.timerRunning()) {
            return;
        }

        context.tickTimer();

        final int remaining = context.remainingSeconds();

        broadcastActionBar(remaining);

        if (remaining <= 0) {
            context.stopTimer();
        }
    }

    public boolean isExpired() {
        return context.timerRunning() && context.remainingSeconds() <= 0;
    }

    public void startTimer(final int seconds) {
        context.startTimer(seconds);
    }

    public void stopTimer() {
        context.stopTimer();
    }

    public void accelerateTimerTo(final int seconds) {
        context.accelerateTimer(seconds);
    }

    public void updateTeamTime(final Team team, final int seconds) {
        context.updateTeamTime(team, seconds);
    }

    public void reset() {
        context.reset();
    }

    private void broadcastActionBar(final int seconds) {
        if (seconds < 0) {
            return;
        }

        final Component feedback;

        if (seconds <= MAX_CACHE_SECONDS) {
            feedback = FEEDBACK_CACHE[seconds];
        } else {
            final int minutes = seconds / 60;
            final int secondsRemainder = seconds % 60;
            final String timeString = String.format("%02d:%02d", minutes, secondsRemainder);

            feedback = Component.text()
                    .append(OPEN_BRACKET)
                    .append(Component.text(timeString, NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(CLOSE_BRACKET)
                    .build();
        }

        for (final UUID playerId : context.participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player == null) {
                continue;
            }

            player.sendActionBar(feedback);
        }
    }
}
