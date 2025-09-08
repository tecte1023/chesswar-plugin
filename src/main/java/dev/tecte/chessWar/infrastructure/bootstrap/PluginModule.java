package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.bootstrap.BoardModule;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * 플러그인 전체의 의존성 주입(DI) 설정을 총괄하는 최상위 Guice 모듈입니다.
 * 각 도메인 모듈을 설치하고, 플러그인 전역에서 사용될 객체들을 제공합니다.
 */
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PluginModule extends AbstractModule {
    private final ChessWar plugin;

    @Override
    protected void configure() {
        bind(ChessWar.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);

        install(new BoardModule());
        install(new CommandModule());
    }

    /**
     * 플러그인의 로거({@link Logger})를 DI 컨테이너에 제공합니다.
     *
     * @param plugin 메인 플러그인 인스턴스
     * @return 플러그인 로거
     */
    @NonNull
    @Provides
    @Singleton
    Logger provideLogger(@NonNull JavaPlugin plugin) {
        return plugin.getLogger();
    }

    /**
     * 사용자 데이터 파일을 관리하는 {@link YmlFileManager}를 DI 컨테이너에 제공합니다.
     *
     * @param plugin 메인 플러그인 인스턴스
     * @return 데이터 파일 관리자 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    YmlFileManager provideDataFileManager(@NonNull JavaPlugin plugin) {
        return new YmlFileManager(plugin, "data.yml");
    }
}
