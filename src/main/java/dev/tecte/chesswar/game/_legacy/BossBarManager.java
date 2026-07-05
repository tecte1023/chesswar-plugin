/*
package dev.tecte.chesswar.game;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class BossBarManager {
    private final GameContext context;
    private BossBar bossBar;

    public void tick() {
        if (!context.timerRunning() || context.currentPhase() == GamePhase.BATTLE) {
            hide();
            return;
        }

        if (context.currentPhase() == GamePhase.PIECE_SELECTION && !context.isSelectionStarted()) {
            hide();
            return;
        }

        final int remaining = context.remainingSeconds();
        final int initial = context.initialSeconds();
        
        if (remaining < 0) {
            hide();
            return;
        }

        final float progress = initial > 0 ? Math.clamp((float) remaining / initial, 0.0f, 1.0f) : 0.0f;
        
        final Component title = Component.text()
                .append(Component.text("남은 시간: ", NamedTextColor.WHITE))
                .append(Component.text(formatTime(remaining), NamedTextColor.GOLD))
                .build();

        if (bossBar == null) {
            bossBar = BossBar.bossBar(title, progress, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        } else {
            bossBar.name(title);
            bossBar.progress(progress);
        }

        bossBar.color(determineColor(progress));

        for (final UUID playerId : context.participantIds()) {
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.showBossBar(bossBar);
            }
        }
    }

    public void hide() {
        if (bossBar == null) {
            return;
        }

        for (final UUID playerId : context.participantIds()) {
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.hideBossBar(bossBar);
            }
        }
        bossBar = null;
    }

    private String formatTime(final int seconds) {
        final int m = seconds / 60;
        final int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private BossBar.Color determineColor(final float progress) {
        if (progress > 0.5f) return BossBar.Color.GREEN;
        if (progress > 0.2f) return BossBar.Color.YELLOW;
        return BossBar.Color.RED;
    }
}
*/
