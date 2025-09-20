package dev.tecte.chessWar.infrastructure.bootstrap;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.command.BoardCommand;
import dev.tecte.chessWar.infrastructure.command.CommandConfigurer;
import dev.tecte.chessWar.infrastructure.command.MainCommand;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.Set;

/**
 * 명령어 관련 의존성 주입(DI) 설정을 담당하는 Guice 모듈입니다.
 * Aikar's Command Framework(ACF)의 {@link PaperCommandManager}를 생성하고, 각 명령어 클래스를 등록합니다.
 */
public class CommandModule extends AbstractModule {
    /**
     * ACF의 {@link PaperCommandManager}를 생성하고 설정하여 DI 컨테이너에 제공합니다.
     *
     * @param plugin       메인 플러그인 인스턴스
     * @param mainCommand  메인 커맨드 핸들러
     * @param boardCommand 보드 커맨드 핸들러
     * @param configurers  각 도메인에서 제공하는 커맨드 설정자의 Set
     * @return 완전히 설정된 {@link PaperCommandManager} 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    @SuppressWarnings("unused")
    public PaperCommandManager provideCommandManager(
            @NonNull ChessWar plugin,
            @NonNull MainCommand mainCommand,
            @NonNull BoardCommand boardCommand,
            @NonNull Set<CommandConfigurer> configurers
    ) {
        PaperCommandManager commandManager = new PaperCommandManager(plugin);

        // 각 도메인이 자신의 커맨드 설정을 스스로 등록하여 모듈 간의 결합도를 낮춤
        for (CommandConfigurer configurer : configurers) {
            configurer.configure(commandManager);
        }

        commandManager.registerCommand(mainCommand);
        commandManager.registerCommand(boardCommand);
        commandManager.enableUnstableAPI("help");

        return commandManager;
    }
}
