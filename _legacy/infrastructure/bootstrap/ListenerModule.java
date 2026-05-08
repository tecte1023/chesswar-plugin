package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.bukkit.event.Listener;

/**
 * 이벤트 리스너 관련 의존성 주입을 설정하는 Guice 모듈입니다.
 */
public class ListenerModule extends AbstractModule {
    @Override
    protected void configure() {
        // 기여하는 리스너가 없더라도 안전한 기동을 위해 빈 집합 주입 보장
        Multibinder.newSetBinder(binder(), Listener.class);
        // 애플리케이션 시작 시점에 리스너들을 Bukkit에 즉시 등록
        bind(SafeListenerRegistrar.class).asEagerSingleton();
    }
}
