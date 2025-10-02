package dev.tecte.chessWar.infrastructure.exception;

import com.google.inject.Inject;
import dev.tecte.chessWar.common.exception.ExceptionHandler;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 발생한 예외를 적절한 {@link ExceptionHandler}에 전달하는 중앙 분배자(Dispatcher) 역할을 합니다.
 * 이 클래스는 등록된 모든 예외 핸들러를 관리하며, 발생한 예외의 타입에 맞는 핸들러를 찾아 실행합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
public class ExceptionDispatcher {
    private final Map<Class<? extends Exception>, ExceptionHandler<?>> handlers;

    /**
     * Guice를 통해 주입된 모든 {@link ExceptionHandler}의 {@link Set}을 받아 내부 맵에 저장합니다.
     *
     * @param exceptionHandlers 등록된 모든 예외 핸들러의 집합
     */
    @Inject
    public ExceptionDispatcher(@NonNull Set<ExceptionHandler<?>> exceptionHandlers) {
        // 빠른 조회를 위해 핸들러 Set을 예외 타입을 키로 하는 Map으로 변환
        handlers = exceptionHandlers.stream()
                .collect(Collectors.toMap(ExceptionHandler::getExceptionType, Function.identity()));
    }

    /**
     * 주어진 예외를 처리할 수 있는 핸들러를 찾아 실행합니다.
     * 만약 적절한 핸들러를 찾지 못하면, 경고 로그를 남깁니다.
     *
     * @param e 처리할 예외 객체
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void dispatch(@NonNull Exception e) {
        ExceptionHandler handler = handlers.get(e.getClass());

        if (handler != null) {
            handler.handle(e);
        } else {
            // 이 분배기에 등록되지 않은 예외는 처리되지 않으므로, 개발자가 핸들러를 추가해야 함을 알림
            log.warn("Unhandled exception occurred: {}", e.getMessage(), e);
        }
    }
}
