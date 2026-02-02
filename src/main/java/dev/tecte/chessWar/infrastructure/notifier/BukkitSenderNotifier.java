package dev.tecte.chessWar.infrastructure.notifier;

import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Singleton;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;

/**
 * Bukkit을 통해 알림 및 안내 메시지를 전송합니다.
 */
@Singleton
public class BukkitSenderNotifier implements SenderNotifier {
    private static final Component PREFIX = Component.text("[ChessWar] ").color(NamedTextColor.GRAY);

    @Override
    public void notifySuccess(@NonNull CommandSender sender, @NonNull Component message) {
        sendMessageWithPrefix(sender, message, NamedTextColor.GREEN);
    }

    @Override
    public void notifySuccess(@NonNull CommandSender sender, @NonNull String message) {
        notifySuccess(sender, Component.text(message));
    }

    @Override
    public void notifyError(@NonNull CommandSender sender, @NonNull Component message) {
        sendMessageWithPrefix(sender, message, NamedTextColor.RED);
    }

    @Override
    public void notifyError(@NonNull CommandSender sender, @NonNull String message) {
        notifyError(sender, Component.text(message));
    }

    @Override
    public void sendTitle(@NonNull CommandSender sender, @NonNull Component title, @NonNull Component subtitle) {
        sender.showTitle(Title.title(title, subtitle));
    }

    @Override
    public void sendTitle(@NonNull CommandSender sender, @NonNull Component title) {
        sendTitle(sender, title, Component.empty());
    }

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
