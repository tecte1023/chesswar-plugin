package dev.tecte.chessWar.infrastructure.notifier;

import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Singleton;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/**
 * {@link SenderNotifier}의 Bukkit API 기반 구현체입니다.
 * Adventure API를 사용하여 플레이어에게 색상이 적용된 메시지를 전송합니다.
 */
@Singleton
public class BukkitSenderNotifier implements SenderNotifier {
    private static final Component PREFIX = Component.text("[ChessWar] ").color(NamedTextColor.GRAY);

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySuccess(@NonNull CommandSender sender, @NonNull Component message) {
        sender.sendMessage(PREFIX.append(message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySuccess(@NonNull CommandSender sender, @NonNull String message) {
        notifySuccess(sender, Component.text(message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyError(@NonNull CommandSender sender, @NonNull Component message) {
        sender.sendMessage(PREFIX.append(message.colorIfAbsent(NamedTextColor.RED)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyError(@NonNull CommandSender sender, @NonNull String message) {
        notifyError(sender, Component.text(message));
    }
}
