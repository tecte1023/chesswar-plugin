package dev.tecte.chessWar.port.exception;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * 예외를 핸들러에게 전달합니다.
 */
public interface ExceptionDispatcher {
    /**
     * 예외 처리를 위임합니다.
     *
     * @param e           발생한 예외
     * @param sender      알림을 받을 대상
     * @param contextInfo 로그에 남길 문맥 정보
     */
    void dispatch(
            @NonNull Exception e,
            @Nullable CommandSender sender,
            @NonNull String contextInfo
    );
}
