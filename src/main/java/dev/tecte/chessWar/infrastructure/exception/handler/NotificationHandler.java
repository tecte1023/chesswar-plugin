package dev.tecte.chessWar.infrastructure.exception.handler;

import dev.tecte.chessWar.common.exception.Notifiable;
import dev.tecte.chessWar.port.exception.ExceptionHandler;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * 사용자 알림이 필요한 예외를 처리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class NotificationHandler implements ExceptionHandler {
    private final SenderNotifier notifier;

    @Override
    public boolean supports(@NonNull Exception e) {
        return e instanceof Notifiable;
    }

    @Override
    public void handle(@NonNull Exception e, @Nullable CommandSender sender) {
        if (sender != null) {
            notifier.notifyError(sender, ((Notifiable) e).userMessage());
        }
    }
}
