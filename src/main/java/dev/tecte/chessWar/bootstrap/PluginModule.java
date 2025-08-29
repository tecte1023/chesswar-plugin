package dev.tecte.chessWar.bootstrap;

import com.google.inject.AbstractModule;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.bootstrap.BoardModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class PluginModule extends AbstractModule {
    private final ChessWar plugin;

    @Override
    protected void configure() {
        bind(ChessWar.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);

        install(new CommandModule());
        install(new BoardModule());
    }
}
