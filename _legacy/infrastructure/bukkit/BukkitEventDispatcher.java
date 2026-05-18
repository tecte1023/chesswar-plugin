package dev.tecte.chessWar.infrastructure.bukkit;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.PluginManager;

/**
 * Bukkit 기반으로 도메인 이벤트를 전파합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitEventDispatcher implements DomainEventDispatcher {
    private final PluginManager pluginManager;

    @Override
    public void dispatch(@NonNull DomainEvent event) {
        pluginManager.callEvent(event);
    }
}
