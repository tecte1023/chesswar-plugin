package dev.tecte.chessWar.common.exception;

import lombok.NonNull;
import net.kyori.adventure.text.Component;

/**
 * 예측 가능한 비즈니스 예외입니다.
 * <p>
 * 이 예외는 시스템 로그를 남기지 않고, 사용자에게 알림 메시지만 전달하는 것을 목적으로 합니다.
 * 생성자에 전달되는 메시지는 사용자에게 직접 노출되므로, 이해하기 쉬운 친화적인 문구를 사용해야 합니다.
 */
public abstract class BusinessException extends RuntimeException implements Notifiable {
    protected BusinessException(@NonNull String message) {
        super(message);
    }

    @NonNull
    @Override
    public Component userMessage() {
        return Component.text(getMessage());
    }
}
