package dev.tecte.chessWar.common.exception;

import lombok.NonNull;
import net.kyori.adventure.text.Component;

/**
 * 사용자 알림이 필요한 예외입니다.
 */
public interface Notifiable {
    /**
     * 사용자에게 전달할 알림 메시지를 반환합니다.
     *
     * @return 알림 메시지
     */
    @NonNull
    Component userMessage();
}
