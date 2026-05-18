package dev.tecte.chessWar.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.LogLevel;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.infrastructure.command.CommandConfigurer;
import dev.tecte.chessWar.infrastructure.command.SafeCommandRegistrar;
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
        // 기여하는 명령어가 없더라도 안전한 기동을 위해 빈 집합 주입 보장
        Multibinder.newSetBinder(binder(), BaseCommand.class);
        // 설정 확장의 유연성을 위해 멀티바인더 구조 채택
        Multibinder.newSetBinder(binder(), CommandConfigurer.class).addBinding().to(SafeCommandRegistrar.class);
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
        PaperCommandManager commandManager = createCommandManager(plugin);

        commandManager.enableUnstableAPI("help");
        commandManager.getLocales().setDefaultLocale(Locale.KOREA);
        commandManager.getLocales().addMessage(Locale.KOREA, MessageKeys.ERROR_PREFIX, "&c[ChessWar] {message}");

        commands.forEach(commandManager::registerCommand);
        configurers.forEach(configurer -> configurer.configure(commandManager));

        return commandManager;
    }

    private PaperCommandManager createCommandManager(ChessWar plugin) {
        return new PaperCommandManager(plugin) {
            @Override
            public void log(LogLevel level, String message, Throwable throwable) {
                // 콘솔의 중복 스택 트레이스 방지를 위해 프레임워크 에러 메시지 차단
                if (level == LogLevel.ERROR && message != null && message.contains("Exception in command")) {
                    return;
                }

                super.log(level, message, throwable);
            }
        };
    }
}
