package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.bukkit.event.Listener;

/**
 * Bukkit 이벤트 리스너의 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 * {@link Multibinder}를 사용하여 플러그인 전체에 흩어져 있는 {@link Listener} 구현체들을 하나의 집합으로 묶습니다.
 * 이렇게 수집된 리스너들은 {@link ListenerRegistrar}에 의해 Bukkit 서버에 일괄 등록됩니다.
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
