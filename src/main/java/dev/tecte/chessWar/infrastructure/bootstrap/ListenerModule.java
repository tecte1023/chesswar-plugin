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
        // Listener를 위한 멀티바인더를 설정하여, 기여하는 모듈이 하나도 없더라도 안전하게 Set<Listener>를 주입받을 수 있도록 보장
        Multibinder.newSetBinder(binder(), Listener.class);
        // ListenerRegistrar를 Eager Singleton으로 등록하여, 애플리케이션 시작 시점에 즉시 리스너들을 Bukkit에 등록하도록 강제
        bind(ListenerRegistrar.class).asEagerSingleton();
    }
}
