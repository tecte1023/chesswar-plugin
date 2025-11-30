package dev.tecte.chessWar.port.exception;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * 발생한 예외를 적절한 핸들러에게 전달하는 책임을 가진 포트 인터페이스입니다.
 */
public interface ExceptionDispatcher {
    /**
     * 예외를 처리 시스템에 전달합니다.
     *
     * @param e           발생한 예외
     * @param sender      명령 실행 주체
     * @param contextInfo 로그에 남길 문맥 정보
     */
    void dispatch(@NonNull Exception e, @Nullable CommandSender sender, @NonNull String contextInfo);
}
