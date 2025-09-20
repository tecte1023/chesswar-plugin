package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.bootstrap.BoardModule;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.team.infrastructure.bootstrap.TeamModule;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
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
        bind(ChessWar.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);

        install(new BoardModule());
        install(new TeamModule());
        install(new CommandModule());
    }

    /**
     * Bukkit의 기본 설정 파일({@code config.yml})에 대한 {@link FileConfiguration} 객체를 DI 컨테이너에 제공합니다.
     * 이 객체는 플러그인의 설정을 읽고 쓰는 데 사용됩니다.
     *
     * @param plugin 메인 플러그인 인스턴스
     * @return {@code config.yml}에 대한 {@link FileConfiguration} 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    public FileConfiguration provideDefaultFileConfiguration(@NonNull JavaPlugin plugin) {
        return plugin.getConfig();
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

    /**
     * Bukkit 서버 인스턴스를 DI 컨테이너에 제공합니다.
     * 서버 인스턴스는 플러그인 전역에서 플레이어, 월드 등 서버 관련 기능에 접근할 때 사용됩니다.
     *
     * @param plugin 메인 플러그인 인스턴스
     * @return {@link Server} 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    public Server provideServer(@NonNull JavaPlugin plugin) {
        return plugin.getServer();
    }

    /**
     * Bukkit 스케줄러를 DI 컨테이너에 제공합니다.
     * 스케줄러는 비동기 작업이나 지연된 작업을 실행할 때 필요합니다.
     *
     * @param server Bukkit 서버 인스턴스
     * @return {@link BukkitScheduler} 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    public BukkitScheduler provideScheduler(@NonNull Server server) {
        return server.getScheduler();
    }
}
