package dev.tecte.chessWar.common.exception;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 시스템 내부 오류나 기술적 문제 발생 시 사용하는 최상위 추상 클래스입니다.
 * <p>
 * 이 예외는 시스템 로그를 상세히 기록하는 것을 목적으로 합니다.
 * 기본적으로 사용자에게는 알림을 보내지 않는 백그라운드 작업이나 내부 오류 처리에 사용합니다.
 */
public abstract class SystemException extends RuntimeException implements Loggable {
    protected SystemException(@NonNull String internalMessage) {
        this(internalMessage, null);
    }

    protected SystemException(@NonNull String internalMessage, @Nullable Throwable cause) {
        super(internalMessage, cause);
    }
}
