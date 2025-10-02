package dev.tecte.chessWar.infrastructure.notifier;

import dev.tecte.chessWar.common.notifier.PlayerNotifier;
import jakarta.inject.Singleton;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * {@link PlayerNotifier}의 Bukkit API 기반 구현체입니다.
 * Adventure API를 사용하여 플레이어에게 색상이 적용된 메시지를 전송합니다.
 */
@Singleton
public class BukkitPlayerNotifier implements PlayerNotifier {
    private static final Component PREFIX = Component.text("[ChessWar] ").color(NamedTextColor.GRAY);

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySuccess(@NonNull UUID playerId, @NonNull String message) {
        sendNotification(playerId, message, NamedTextColor.GREEN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyError(@NonNull UUID playerId, @NonNull String message) {
        sendNotification(playerId, message, NamedTextColor.RED);
    }

    private void sendNotification(@NonNull UUID playerId, @NonNull String message, @NonNull TextColor color) {
        Player player = Bukkit.getPlayer(playerId);

        if (player == null) {
            return;
        }

        Component messageComponent = PREFIX.append(Component.text(message).color(color));

        player.sendMessage(messageComponent);
    }
}
