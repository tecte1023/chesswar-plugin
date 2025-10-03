package dev.tecte.chessWar.infrastructure.exception;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.Nullable;

/**
 * {@code @HandleExceptions} 어노테이션이 붙은 메서드 호출을 가로채는 AOP 인터셉터입니다.
 * 메서드 실행 중 발생하는 모든 {@link Exception}을 잡아 {@link ExceptionDispatcher}로 전달하여
 * 중앙에서 예외를 처리하도록 합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExceptionHandlingInterceptor implements MethodInterceptor {
    private final Provider<ExceptionDispatcher> exceptionDispatcherProvider;

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Exception e) {
            // 복구 가능한 예외는 중앙 핸들러로 보내 처리
            exceptionDispatcherProvider.get().dispatch(e);

            return null;
        }
    }
}
