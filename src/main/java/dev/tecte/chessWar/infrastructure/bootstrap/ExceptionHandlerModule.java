package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.infrastructure.exception.DefaultExceptionDispatcher;
import dev.tecte.chessWar.infrastructure.exception.ExceptionHandlingInterceptor;
import dev.tecte.chessWar.infrastructure.exception.handler.LoggingHandler;
import dev.tecte.chessWar.infrastructure.exception.handler.NotificationHandler;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import dev.tecte.chessWar.port.exception.ExceptionHandler;

/**
 * 예외 처리 관련 의존성 주입을 설정하는 Guice 모듈입니다.
 */
public class ExceptionHandlerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ExceptionDispatcher.class).to(DefaultExceptionDispatcher.class);

        var interceptor = new ExceptionHandlingInterceptor();

        // Interceptor는 생성자 주입이 불가능하므로 필드 주입
        requestInjection(interceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(HandleException.class), interceptor);

        var exceptionHandlerMultibinder = Multibinder.newSetBinder(binder(), ExceptionHandler.class);

        exceptionHandlerMultibinder.addBinding().to(NotificationHandler.class);
        exceptionHandlerMultibinder.addBinding().to(LoggingHandler.class);
    }
}
