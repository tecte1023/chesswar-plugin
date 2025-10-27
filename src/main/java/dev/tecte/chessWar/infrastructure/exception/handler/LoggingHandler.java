package dev.tecte.chessWar.infrastructure.exception.handler;

import dev.tecte.chessWar.common.exception.Loggable;
import dev.tecte.chessWar.port.exception.ExceptionHandler;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Loggable} 인터페이스를 구현한 예외를 처리하여 로그를 남기는 핸들러입니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
public class LoggingHandler implements ExceptionHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(@NonNull Exception e) {
        return e instanceof Loggable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull Exception e, @Nullable CommandSender sender) {
        log.warn(e.getMessage());
    }
}
