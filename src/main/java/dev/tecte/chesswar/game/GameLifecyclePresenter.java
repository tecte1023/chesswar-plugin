package dev.tecte.chesswar.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GameLifecyclePresenter {
    public void showGameStopFeedback(@NotNull final Player player) {
        final var message = Component.text("관리자에 의해 게임이 강제 중단되었습니다.", NamedTextColor.RED);

        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 1.0f, 1.0f);
    }
}
