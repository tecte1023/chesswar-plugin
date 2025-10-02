package dev.tecte.chessWar.common.exception;

import lombok.NonNull;

/**
 * 특정 종류의 예외 {@code T}를 처리하는 핸들러의 공통 규약을 정의하는 인터페이스입니다.
 * 이 핸들러들은 {@code ExceptionDispatcher}에 의해 관리되며, 발생한 예외 타입에 맞는 핸들러가 호출됩니다.
 *
 * @param <T> 처리할 예외의 타입
 */
public interface ExceptionHandler<T extends Exception> {
    /**
     * 주어진 예외를 처리합니다.
     *
     * @param e 처리할 예외 객체
     */
    void handle(@NonNull T e);

    /**
     * 이 핸들러가 처리하는 예외의 {@link Class} 타입을 반환합니다.
     * 이 정보는 {@code ExceptionDispatcher}가 적절한 핸들러를 찾는 데 사용됩니다.
     *
     * @return 처리할 예외의 클래스 타입
     */
    @NonNull
    Class<T> getExceptionType();
}
