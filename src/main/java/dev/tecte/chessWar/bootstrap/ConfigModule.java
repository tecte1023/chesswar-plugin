package dev.tecte.chessWar.bootstrap;

import com.google.inject.AbstractModule;
import dev.tecte.chessWar.ChessWar;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;

@RequiredArgsConstructor
public class ConfigModule extends AbstractModule {
    private final ChessWar plugin;

    @Override
    protected void configure() {
        bind(FileConfiguration.class).toInstance(plugin.getConfig());
    }
}
