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
 * 명령어 관련 의존성 주입 설정을 총괄하는 Guice 모듈입니다.
 * Guice의 Multibinder를 통해 여러 모듈에 흩어져 등록된 구현체들을 수집하여
 * {@link PaperCommandManager}를 설정하고 초기화하는 역할을 담당합니다.
 */
public class CommandModule extends AbstractModule {
    @Override
    protected void configure() {
        // 여러 모듈에서 각자 자신의 Command와 Configurer를 독립적으로 등록할 수 있도록 Set 바인딩을 준비
        Multibinder.newSetBinder(binder(), BaseCommand.class);
        Multibinder.newSetBinder(binder(), CommandConfigurer.class);
    }

    /**
     * ACF의 {@link PaperCommandManager}를 생성하고, 수집된 모든 커맨드와 설정자를 주입하여 초기화한 후 DI 컨테이너에 제공합니다.
     *
     * @param plugin      메인 플러그인 인스턴스
     * @param commands    Multibinder를 통해 모든 모듈에서 수집된 {@link BaseCommand} 구현체의 Set
     * @param configurers Multibinder를 통해 모든 모듈에서 수집된 {@link CommandConfigurer} 구현체의 Set
     * @return 완전히 설정된 {@link PaperCommandManager} 싱글턴 인스턴스
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
        // Multibinder로 모인 모든 커맨드를 자동으로 등록
        commands.forEach(commandManager::registerCommand);
        // 각 도메인이 자신의 커맨드 설정을 스스로 등록하여 모듈 간의 결합도를 낮춤
        configurers.forEach(configurer -> configurer.configure(commandManager));

        return commandManager;
    }
}
