package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.common.exception.ExceptionHandler;
import dev.tecte.chessWar.common.exception.PlayerNotificationHandler;

/**
 * 애플리케이션 전역에서 사용될 예외 핸들러들의 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 * {@link Multibinder}를 사용하여 각 도메인에서 정의된 {@link ExceptionHandler} 구현체들을 등록합니다.
 */
public class ExceptionHandlerModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<ExceptionHandler<?>> handlerBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<>() {
        });

        handlerBinder.addBinding().to(PlayerNotificationHandler.class);
    }
}
