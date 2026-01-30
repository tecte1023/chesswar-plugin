package dev.tecte.chessWar.common.exception;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * 알림이 필요한 시스템 예외입니다.
 * <p>
 * 이 예외는 시스템 로그를 기록하고, 동시에 사용자에게도 알림 메시지를 전달합니다.
 * 주로 사용자의 흐름을 중단시켜야 하는 심각한 기술적 오류 상황에서 사용합니다.
 */
public abstract class NotifiableSystemException extends SystemException implements Notifiable {
    private final String userMessage;

    protected NotifiableSystemException(@NonNull String internalMessage, @NonNull String userMessage) {
        this(internalMessage, userMessage, null);
    }

    protected NotifiableSystemException(
            @NonNull String internalMessage,
            @NonNull String userMessage,
            @Nullable Throwable cause
    ) {
        super(internalMessage, cause);
        this.userMessage = userMessage;
    }

    @NonNull
    @Override
    public Component userMessage() {
        return Component.text(userMessage);
    }
}
