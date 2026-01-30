package dev.tecte.chessWar.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.infrastructure.command.CommandConfigurer;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.Locale;
import java.util.Set;

/**
 * 명령어 관련 의존성 주입을 설정하는 Guice 모듈입니다.
 */
public class CommandModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), BaseCommand.class);
        Multibinder.newSetBinder(binder(), CommandConfigurer.class);
    }

    /**
     * PaperCommandManager를 생성하여 제공합니다.
     *
     * @param plugin      메인 플러그인
     * @param commands    등록할 명령어 목록
     * @param configurers 명령어 설정자 목록
     * @return 명령어 매니저
     */
    @NonNull
    @Provides
    @Singleton
    @SuppressWarnings("unused")
    public PaperCommandManager provideCommandManager(
            @NonNull ChessWar plugin,
            @NonNull Set<BaseCommand> commands,
            @NonNull Set<CommandConfigurer> configurers
    ) {
        PaperCommandManager commandManager = new PaperCommandManager(plugin);

        commandManager.enableUnstableAPI("help");
        commandManager.getLocales().setDefaultLocale(Locale.KOREA);
        commandManager.getLocales().addMessage(Locale.KOREA, MessageKeys.ERROR_PREFIX, "&c[ChessWar] {message}");
        commands.forEach(commandManager::registerCommand);
        configurers.forEach(configurer -> configurer.configure(commandManager));

        return commandManager;
    }
}
