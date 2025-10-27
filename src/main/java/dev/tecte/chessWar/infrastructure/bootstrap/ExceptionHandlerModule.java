package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.infrastructure.exception.ExceptionHandlingInterceptor;
import dev.tecte.chessWar.port.exception.ExceptionHandler;
import dev.tecte.chessWar.infrastructure.exception.handler.LoggingHandler;
import dev.tecte.chessWar.infrastructure.exception.handler.NotificationHandler;

/**
 * 예외 처리와 관련된 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 * AOP를 활용한 예외 처리 인터셉터와, 다양한 예외 처리 핸들러들을 바인딩합니다.
 */
public class ExceptionHandlerModule extends AbstractModule {
    @Override
    protected void configure() {
        ExceptionHandlingInterceptor interceptor = new ExceptionHandlingInterceptor();

        requestInjection(interceptor);
        bindInterceptor(
                Matchers.any(),
                Matchers.annotatedWith(HandleException.class),
                interceptor
        );

        Multibinder<ExceptionHandler> exceptionHandlerMultibinder = Multibinder.newSetBinder(binder(), ExceptionHandler.class);

        exceptionHandlerMultibinder.addBinding().to(NotificationHandler.class);
        exceptionHandlerMultibinder.addBinding().to(LoggingHandler.class);
    }
}
