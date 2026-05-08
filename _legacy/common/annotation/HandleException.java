package dev.tecte.chessWar.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 예외 발생 시 중앙 처리 로직을 적용할 메서드임을 나타냅니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandleException {
    /**
     * boolean 반환 메서드 실패 시 반환할 값을 설정합니다.
     */
    boolean fallbackBoolean() default false;

    /**
     * 정수형 반환 메서드 실패 시 반환할 값을 설정합니다.
     */
    int fallbackInt() default 0;

    /**
     * 실수형 반환 메서드 실패 시 반환할 값을 설정합니다.
     */
    double fallbackNumber() default 0.0;
}
