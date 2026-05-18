package dev.tecte.chessWar.infrastructure.exception.handler;

import dev.tecte.chessWar.common.exception.Loggable;
import dev.tecte.chessWar.port.exception.ExceptionHandler;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * 로그 기록이 필요한 예외를 처리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
public class LoggingHandler implements ExceptionHandler {
    @Override
    public boolean supports(@NonNull Exception e) {
        return e instanceof Loggable;
    }

    @Override
    public void handle(@NonNull Exception e, @Nullable CommandSender sender) {
        log.atLevel(((Loggable) e).logLevel())
                .setCause(e)
                .log(e.getMessage());
    }
}
