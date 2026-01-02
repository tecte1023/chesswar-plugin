package dev.tecte.chessWar.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.bootstrap.BoardModule;
import dev.tecte.chessWar.game.infrastructure.bootstrap.GameModule;
import dev.tecte.chessWar.piece.infrastructure.bootstrap.PieceModule;
import dev.tecte.chessWar.infrastructure.command.MainCommand;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.notifier.BukkitSenderNotifier;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import dev.tecte.chessWar.team.infrastructure.bootstrap.TeamModule;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * 플러그인 전체의 의존성 주입 설정을 총괄하는 최상위 Guice 모듈입니다.
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
        bind(BukkitScheduler.class).toInstance(server.getScheduler());
        bind(PluginManager.class).toInstance(server.getPluginManager());
        bind(SenderNotifier.class).to(BukkitSenderNotifier.class);

        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(MainCommand.class);

        install(new BoardModule());
        install(new TeamModule());
        install(new PieceModule());
        install(new GameModule());
        install(new CommandModule());
        install(new ListenerModule());
        install(new ExceptionHandlerModule());
    }

    /**
     * 사용자 데이터 파일(data.yml)을 관리하는 {@link YmlFileManager}의 싱글턴 인스턴스를 생성하여 DI 컨테이너에 제공합니다.
     *
     * @param plugin 메인 플러그인 인스턴스
     * @return {@link YmlFileManager}의 싱글턴 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    public YmlFileManager provideDataFileManager(@NonNull JavaPlugin plugin) {
        return new YmlFileManager(plugin, "data.yml");
    }
}
