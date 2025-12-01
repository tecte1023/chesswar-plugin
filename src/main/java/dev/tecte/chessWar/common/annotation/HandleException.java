package dev.tecte.chessWar.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 어노테이션이 붙은 메서드에서 발생하는 예외를 AOP 인터셉터가 가로채서 중앙에서 처리하도록 지시합니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandleException {
    /**
     * boolean 반환 메서드 실패 시 반환할 값
     */
    boolean fallbackBoolean() default false;

    /**
     * 정수형 반환 메서드 실패 시 반환할 값
     */
    int fallbackInt() default 0;

    /**
     * 실수형 반환 메서드 실패 시 반환할 값
     */
    double fallbackNumber() default 0.0;
}
