package dev.tecte.chessWar.infrastructure.config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ConfigManager {
    private final Set<ConfigUpdater> updaters;

    @Getter
    private volatile PluginConfig pluginConfig;

    public void load() {
        PluginConfig newConfig = PluginConfig.empty();

        for (ConfigUpdater updater : updaters) {
            newConfig = updater.update(newConfig);
        }

        pluginConfig = newConfig;
    }
}
