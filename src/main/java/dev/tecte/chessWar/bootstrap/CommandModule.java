package dev.tecte.chessWar.bootstrap;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.command.BoardCommand;
import dev.tecte.chessWar.command.MainCommand;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CommandModule extends AbstractModule {
    @NotNull
    @Provides
    @Singleton
    public PaperCommandManager provideCommandManager(
            @NotNull ChessWar plugin,
            @NotNull MainCommand mainCommand,
            @NotNull BoardCommand boardCommand
    ) {
        PaperCommandManager commandManager = new PaperCommandManager(plugin);

        commandManager.enableUnstableAPI("help");
        commandManager.registerCommand(mainCommand);
        commandManager.registerCommand(boardCommand);

        return commandManager;
    }
}
