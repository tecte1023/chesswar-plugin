package dev.tecte.chessWar.infrastructure.bootstrap;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.command.BoardCommand;
import dev.tecte.chessWar.infrastructure.command.MainCommand;
import jakarta.inject.Singleton;
import lombok.NonNull;

/**
 * 명령어 관련 의존성 주입(DI) 설정을 담당하는 Guice 모듈입니다.
 * Aikar's Command Framework(ACF)의 {@link PaperCommandManager}를 생성하고, 각 명령어 클래스를 등록합니다.
 */
public class CommandModule extends AbstractModule {
    @NonNull
    @Provides
    @Singleton
    @SuppressWarnings("unused")
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
