package dev.tecte.chessWar.infrastructure.bootstrap;

import dev.tecte.chessWar.ChessWar;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.Set;

/**
 * Guice가 주입해준 모든 Listener들을 Bukkit 플러그인 매니저에 등록하는 역할을 수행합니다.
 * Eager Singleton으로 바인딩되어 애플리케이션 시작 시 즉시 실행됩니다.
 */
@Singleton
public class ListenerRegistrar {
    /**
     * 모든 {@link Listener}를 Bukkit {@link PluginManager}에 등록합니다.
     *
     * @param plugin      메인 플러그인 인스턴스
     * @param pluginManager Bukkit의 플러그인 매니저
     * @param listeners   Guice에 의해 주입된 모든 리스너의 Set
     */
    @Inject
    public ListenerRegistrar(
            @NonNull ChessWar plugin,
            @NonNull PluginManager pluginManager,
            @NonNull Set<Listener> listeners
    ) {
        listeners.forEach(listener -> pluginManager.registerEvents(listener, plugin));
    }
}
