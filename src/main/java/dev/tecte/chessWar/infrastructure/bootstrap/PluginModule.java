package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.bootstrap.BoardModule;
import dev.tecte.chessWar.common.annotation.HandleExceptions;
import dev.tecte.chessWar.common.notifier.PlayerNotifier;
import dev.tecte.chessWar.infrastructure.exception.ExceptionDispatcher;
import dev.tecte.chessWar.infrastructure.exception.ExceptionHandlingInterceptor;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.notifier.BukkitPlayerNotifier;
import dev.tecte.chessWar.team.infrastructure.bootstrap.TeamModule;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

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
        Server server = plugin.getServer();

        bind(ChessWar.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);
        bind(FileConfiguration.class).toInstance(plugin.getConfig());
        bind(BukkitScheduler.class).toInstance(server.getScheduler());
        bind(PluginManager.class).toInstance(server.getPluginManager());
        bind(PlayerNotifier.class).to(BukkitPlayerNotifier.class);

        install(new BoardModule());
        install(new TeamModule());
        install(new CommandModule());
        install(new ListenerModule());
        install(new ExceptionHandlerModule());

        bindInterceptor(
                Matchers.any(),
                Matchers.annotatedWith(HandleExceptions.class),
                new ExceptionHandlingInterceptor(getProvider(ExceptionDispatcher.class))
        );
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
    public YmlFileManager provideDataFileManager(@NonNull JavaPlugin plugin) {
        return new YmlFileManager(plugin, "data.yml");
    }
}
