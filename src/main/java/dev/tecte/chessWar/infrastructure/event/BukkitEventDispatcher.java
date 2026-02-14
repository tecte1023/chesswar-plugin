package dev.tecte.chessWar.infrastructure.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.PluginManager;

/**
 * 도메인 이벤트를 Bukkit 이벤트 시스템으로 전파합니다.
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
