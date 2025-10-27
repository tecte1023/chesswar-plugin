package dev.tecte.chessWar.port.exception;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * 예외 처리 로직을 캡슐화하는 핸들러의 공통 인터페이스입니다.
 */
public interface ExceptionHandler {
    /**
     * 이 핸들러가 주어진 예외를 처리할 수 있는지 여부를 반환합니다.
     *
     * @param e 확인할 예외
     * @return 처리할 수 있으면 true, 그렇지 않으면 false
     */
    boolean supports(@NonNull Exception e);

    /**
     * 예외 처리 로직을 수행합니다.
     *
     * @param e      처리할 예외
     * @param sender 명령어를 실행한 주체
     */
    void handle(@NonNull Exception e, @Nullable CommandSender sender);
}
