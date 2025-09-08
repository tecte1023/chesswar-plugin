package dev.tecte.chessWar.infrastructure.config;

@FunctionalInterface
public interface ConfigUpdater {
    PluginConfig update(PluginConfig currentConfig);
}
