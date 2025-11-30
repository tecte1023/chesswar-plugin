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

import java.util.List;
import java.util.Set;

/**
 * 예외 처리 로직의 단일 진실 공급원입니다.
 * 예외가 발생했을 때 적절한 핸들러를 찾아 실행하는 역할만 전담합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultExceptionDispatcher implements ExceptionDispatcher {
    private final Provider<Set<ExceptionHandler>> handlersProvider;
    private final SenderNotifier notifier;

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatch(@NonNull Exception e, @Nullable CommandSender sender, @NonNull String contextInfo) {
        Set<ExceptionHandler> handlers = handlersProvider.get();
        List<ExceptionHandler> supportingHandlers = handlers.stream()
                .filter(handler -> handler.supports(e))
                .toList();

        if (supportingHandlers.isEmpty()) {
            log.error("Unhandled exception caught in {}:", contextInfo, e);

            if (sender != null) {
                notifier.notifyError(sender, "알 수 없는 오류가 발생했습니다.");
            }
        } else {
            supportingHandlers.forEach(handler -> handler.handle(e, sender));
        }
    }
}
