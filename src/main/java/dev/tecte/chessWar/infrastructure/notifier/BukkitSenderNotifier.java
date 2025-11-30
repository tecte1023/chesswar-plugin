package dev.tecte.chessWar.infrastructure.notifier;

import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Singleton;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
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
        sendMessageWithPrefix(sender, message, NamedTextColor.GREEN);
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
        sendMessageWithPrefix(sender, message, NamedTextColor.RED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyError(@NonNull CommandSender sender, @NonNull String message) {
        notifyError(sender, Component.text(message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendTitle(@NonNull CommandSender sender, @NonNull Component title, @NonNull Component subtitle) {
        sender.showTitle(Title.title(title, subtitle));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendTitle(@NonNull CommandSender sender, @NonNull Component title) {
        sendTitle(sender, title, Component.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendActionBar(@NonNull CommandSender sender, @NonNull Component message) {
        sender.sendActionBar(message);
    }

    private void sendMessageWithPrefix(
            @NonNull CommandSender sender,
            @NonNull Component message,
            @NonNull NamedTextColor color
    ) {
        sender.sendMessage(PREFIX.append(message.colorIfAbsent(color)));
    }
}
