package dev.tecte.chessWar.infrastructure.bukkit;

import dev.tecte.chessWar.port.UserNotifier;
import dev.tecte.chessWar.port.UserResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;

import java.util.UUID;

/**
 * Bukkit 기반으로 알림 및 안내 메시지를 전송합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitUserNotifier implements UserNotifier {
    private static final Component PREFIX = Component.text("[ChessWar] ").color(NamedTextColor.GRAY);
    private final UserResolver userResolver;

    @Override
    public void inform(@NonNull UUID recipientId, @NonNull Component message) {
        sendMessageWithPrefix(userResolver.resolveSender(recipientId), message);
    }

    @Override
    public void inform(@NonNull CommandSender recipient, @NonNull Component message) {
        sendMessageWithPrefix(recipient, message);
    }

    @Override
    public void informSuccess(@NonNull UUID recipientId, @NonNull Component message) {
        sendMessageWithPrefix(userResolver.resolveSender(recipientId), message, NamedTextColor.GREEN);
    }

    @Override
    public void informSuccess(@NonNull CommandSender recipient, @NonNull Component message) {
        sendMessageWithPrefix(recipient, message, NamedTextColor.GREEN);
    }

    @Override
    public void informError(@NonNull UUID recipientId, @NonNull Component message) {
        sendMessageWithPrefix(userResolver.resolveSender(recipientId), message, NamedTextColor.RED);
    }

    @Override
    public void informError(@NonNull CommandSender recipient, @NonNull Component message) {
        sendMessageWithPrefix(recipient, message, NamedTextColor.RED);
    }

    @Override
    public void displayTitle(@NonNull UUID recipientId, @NonNull Component title) {
        displayTitle(recipientId, title, Component.empty());
    }

    @Override
    public void displayTitle(@NonNull CommandSender recipient, @NonNull Component title) {
        displayTitle(recipient, title, Component.empty());
    }

    @Override
    public void displayTitle(@NonNull UUID recipientId, @NonNull Component title, @NonNull Component subtitle) {
        userResolver.resolveSender(recipientId).showTitle(Title.title(title, subtitle));
    }

    @Override
    public void displayTitle(@NonNull CommandSender recipient, @NonNull Component title, @NonNull Component subtitle) {
        recipient.showTitle(Title.title(title, subtitle));
    }

    @Override
    public void displayActionBar(@NonNull UUID recipientId, @NonNull Component message) {
        userResolver.resolveSender(recipientId).sendActionBar(message);
    }

    @Override
    public void displayActionBar(@NonNull CommandSender recipient, @NonNull Component message) {
        recipient.sendActionBar(message);
    }

    private void sendMessageWithPrefix(
            @NonNull CommandSender sender,
            @NonNull Component message,
            @NonNull NamedTextColor color
    ) {
        sendMessageWithPrefix(sender, message.colorIfAbsent(color));
    }

    private void sendMessageWithPrefix(@NonNull CommandSender sender, @NonNull Component message) {
        sender.sendMessage(PREFIX.append(message));
    }
}
