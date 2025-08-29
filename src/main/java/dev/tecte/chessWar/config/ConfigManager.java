package dev.tecte.chessWar.config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;

@Singleton
public class ConfigManager {
    private final ConfigLoader loader;

    @Getter
    private volatile PluginConfig pluginConfig;

    @Inject
    public ConfigManager(ConfigLoader loader) {
        this.loader = loader;
        this.reload();
    }

    public void reload() {
        pluginConfig = loader.load();
    }
}
