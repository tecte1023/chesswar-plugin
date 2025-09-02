package dev.tecte.chessWar.bootstrap;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.command.BoardCommand;
import dev.tecte.chessWar.command.MainCommand;
import jakarta.inject.Singleton;
import lombok.NonNull;

public class CommandModule extends AbstractModule {
    @Provides
    @Singleton
    public PaperCommandManager provideCommandManager(
            @NonNull ChessWar plugin,
            @NonNull MainCommand mainCommand,
            @NonNull BoardCommand boardCommand
    ) {
        PaperCommandManager commandManager = new PaperCommandManager(plugin);

        commandManager.enableUnstableAPI("help");
        commandManager.registerCommand(mainCommand);
        commandManager.registerCommand(boardCommand);

        return commandManager;
    }
}
