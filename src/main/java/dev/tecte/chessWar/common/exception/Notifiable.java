package dev.tecte.chessWar.common.exception;

import lombok.NonNull;
import net.kyori.adventure.text.Component;

/**
 * 이 인터페이스를 구현하는 예외는 발생 시 사용자에게 알려야 함을 나타냅니다.
 */
public interface Notifiable {
    /**
     * 예외의 상세 메시지를 반환합니다. 모든 예외 클래스는 이 메서드를 구현하고 있습니다.
     *
     * @return 예외 메시지 문자열
     */
    @NonNull
    String getMessage();

    /**
     * 사용자에게 알림으로 보낼 {@link Component}를 반환합니다.
     *
     * @return 사용자 알림용 Component
     */
    @NonNull
    default Component getNotificationComponent() {
        return Component.text(getMessage());
    }
}
