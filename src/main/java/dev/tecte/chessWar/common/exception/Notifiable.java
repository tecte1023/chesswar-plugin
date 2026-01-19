package dev.tecte.chessWar.common.exception;

import lombok.NonNull;
import net.kyori.adventure.text.Component;

/**
 * 이 인터페이스를 구현하는 예외는 발생 시 사용자에게 알려야 함을 나타냅니다.
 */
public interface Notifiable {
    /**
     * 사용자에게 알림으로 보낼 {@link Component}를 반환합니다.
     *
     * @return 사용자 알림용 {@link Component}
     */
    @NonNull
    Component userMessage();
}
