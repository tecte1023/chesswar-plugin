package dev.tecte.chessWar.config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class ConfigManager {
    private final ConfigLoader loader;

    @Getter
    private volatile PluginConfig pluginConfig;

    public void reload() {
        pluginConfig = loader.load();
    }
}
