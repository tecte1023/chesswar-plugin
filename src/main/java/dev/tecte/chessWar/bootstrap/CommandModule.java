package dev.tecte.chessWar.bootstrap;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CommandModule extends AbstractModule {
    @NotNull
    @Provides
    @Singleton
    public PaperCommandManager provideCommandManager(@NotNull ChessWar plugin, @NotNull MainCommand mainCommand) {
        PaperCommandManager commandManager = new PaperCommandManager(plugin);

        commandManager.registerCommand(mainCommand);

        return commandManager;
    }
}
