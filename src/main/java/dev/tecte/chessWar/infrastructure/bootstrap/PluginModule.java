package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.bootstrap.BoardModule;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class PluginModule extends AbstractModule {
    private final ChessWar plugin;

    @Override
    protected void configure() {
        bind(ChessWar.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);

        install(new BoardModule());
        install(new CommandModule());
    }

    @NonNull
    @Provides
    @Singleton
    Logger provideLogger(@NonNull JavaPlugin plugin) {
        return plugin.getLogger();
    }

    @NonNull
    @Provides
    @Singleton
    YmlFileManager provideDataFileManager(@NonNull JavaPlugin plugin) {
        return new YmlFileManager(plugin, "data.yml");
    }
}
