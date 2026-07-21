package dev.tecte.chesswar.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public final class StartingPhasePresenter {
    private static final Times COUNTDOWN_TIMES = Times.times(
            Duration.ZERO,
            Duration.ofMillis(1100),
            Duration.ZERO
    );
    private static final Times START_TIMES = Times.times(
            Duration.ofMillis(200),
            Duration.ofSeconds(2),
            Duration.ofMillis(500)
    );

    public void showCountdown(@NotNull final Player player, final int secondsLeft) {
        final var mainTitle = Component.text(String.valueOf(secondsLeft), NamedTextColor.AQUA, TextDecoration.BOLD);
        final var subtitle = Component.empty();
        final var title = Title.title(mainTitle, subtitle, COUNTDOWN_TIMES);

        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public void showGameStartFeedback(@NotNull final Player player) {
        final var mainTitle = Component.text("병력 편성", NamedTextColor.GOLD, TextDecoration.BOLD);
        final var subtitle = Component.text("플레이할 기물을 선택하세요", NamedTextColor.GRAY);
        final var title = Title.title(mainTitle, subtitle, START_TIMES);

        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }
}
