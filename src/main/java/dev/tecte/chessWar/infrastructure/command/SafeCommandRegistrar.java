package dev.tecte.chessWar.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.RegisteredCommand;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 명령어 실행 중 발생하는 예외를 전역 예외 처리기로 위임합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SafeCommandRegistrar implements CommandConfigurer {
    private final ExceptionDispatcher exceptionDispatcher;

    @Override
    public void configure(@NonNull PaperCommandManager commandManager) {
        commandManager.setDefaultExceptionHandler(this::handleException);
    }

    private boolean handleException(
            @NonNull BaseCommand command,
            @NonNull RegisteredCommand<?> registeredCommand,
            @NonNull CommandIssuer sender,
            @NonNull List<String> args,
            @NonNull Throwable t
    ) {
        Throwable cause = t.getCause() != null ? t.getCause() : t;

        if (cause instanceof Exception e) {
            exceptionDispatcher.dispatch(e, sender.getIssuer(), "Command " + registeredCommand.getCommand());

            return true;
        }

        return false;
    }
}
