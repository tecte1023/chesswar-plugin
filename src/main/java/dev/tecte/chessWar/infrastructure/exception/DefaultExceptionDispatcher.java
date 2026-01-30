package dev.tecte.chessWar.infrastructure.exception;

import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import dev.tecte.chessWar.port.exception.ExceptionHandler;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 예외를 핸들러에게 전달합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultExceptionDispatcher implements ExceptionDispatcher {
    private final Provider<Set<ExceptionHandler>> handlersProvider;
    private final SenderNotifier notifier;

    @Override
    public void dispatch(
            @NonNull Exception e,
            @Nullable CommandSender sender,
            @NonNull String contextInfo
    ) {
        boolean handled = false;

        for (ExceptionHandler handler : handlersProvider.get()) {
            if (handler.supports(e)) {
                handler.handle(e, sender);
                handled = true;
            }
        }

        if (!handled) {
            log.error("Unhandled exception caught in {}:", contextInfo, e);

            if (sender != null) {
                notifier.notifyError(sender, "알 수 없는 오류가 발생했습니다.");
            }
        }
    }
}
