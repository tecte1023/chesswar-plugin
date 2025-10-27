package dev.tecte.chessWar.common.exception;

import lombok.NonNull;

/**
 * 비즈니스 로직상에서 발생하는 예측 가능한 예외를 나타내는 최상위 추상 클래스입니다.
 * <p>
 * 이 클래스를 상속하는 구체적인 예외 클래스는 {@link Notifiable}, {@link Loggable} 같은 마커 인터페이스를 구현하여
 * AOP 인터셉터가 예외를 어떻게 처리할지 결정하게 합니다.
 */
public abstract class BusinessException extends RuntimeException {
    public BusinessException(@NonNull String message) {
        super(message);
    }
}
