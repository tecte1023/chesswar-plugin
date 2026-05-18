package dev.tecte.chessWar.port.exception;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * 예외 처리 로직을 정의하는 핸들러 인터페이스입니다.
 */
public interface ExceptionHandler {
    /**
     * 예외 처리 가능 여부를 확인합니다.
     *
     * @param e 확인할 예외
     * @return 처리 가능 여부
     */
    boolean supports(@NonNull Exception e);

    /**
     * 예외를 처리합니다.
     *
     * @param e      처리할 예외
     * @param sender 알림을 받을 대상
     */
    void handle(@NonNull Exception e, @Nullable CommandSender sender);
}
