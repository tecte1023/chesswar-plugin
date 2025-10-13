package dev.tecte.chessWar.infrastructure.exception;

import com.google.inject.Inject;
import dev.tecte.chessWar.common.exception.ExceptionHandler;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

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
     * @param exceptionHandlers 등록할 예외 핸들러의 Set
     */
    @Inject
    public ExceptionDispatcher(@NonNull Set<ExceptionHandler<?>> exceptionHandlers) {
        handlers = exceptionHandlers.stream()
                .collect(Collectors.toMap(ExceptionHandler::getExceptionType, Function.identity()));
    }

    /**
     * 발생한 예외를 가장 적합한 핸들러에게 전달합니다.
     *
     * @param e 처리할 예외
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void dispatch(@NonNull Exception e) {
        ExceptionHandler handler = findHandler(e.getClass());

        if (handler != null) {
            handler.handle(e);
        } else {
            log.warn("Unhandled exception occurred: {}", e.getMessage(), e);
        }
    }

    @Nullable
    private ExceptionHandler<?> findHandler(@NonNull Class<?> exceptionType) {
        Class<?> currentType = exceptionType;
        ExceptionHandler<?> handler = null;

        while (handler == null && currentType != null && Exception.class.isAssignableFrom(currentType)) {
            handler = handlers.get(currentType);
            currentType = currentType.getSuperclass();
        }

        return handler;
    }
}
