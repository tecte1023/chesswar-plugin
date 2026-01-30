package dev.tecte.chessWar.infrastructure.bootstrap;

import dev.tecte.chessWar.ChessWar;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.Set;

/**
 * 모든 리스너를 Bukkit에 등록합니다.
 */
@Singleton
public class ListenerRegistrar {
    @Inject
    public ListenerRegistrar(
            @NonNull ChessWar plugin,
            @NonNull PluginManager pluginManager,
            @NonNull Set<Listener> listeners
    ) {
        listeners.forEach(listener -> pluginManager.registerEvents(listener, plugin));
    }
}
