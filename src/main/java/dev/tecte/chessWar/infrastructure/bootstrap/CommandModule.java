package dev.tecte.chessWar.infrastructure.bootstrap;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.command.BoardCommand;
import dev.tecte.chessWar.infrastructure.command.MainCommand;
import jakarta.inject.Singleton;
import lombok.NonNull;

public class CommandModule extends AbstractModule {
    @NonNull
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
