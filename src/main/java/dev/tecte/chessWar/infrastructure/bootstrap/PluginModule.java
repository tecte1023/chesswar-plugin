package dev.tecte.chessWar.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.bootstrap.BoardModule;
import dev.tecte.chessWar.game.infrastructure.bootstrap.GameModule;
import dev.tecte.chessWar.infrastructure.command.MainCommand;
import dev.tecte.chessWar.infrastructure.notifier.BukkitSenderNotifier;
import dev.tecte.chessWar.piece.infrastructure.bootstrap.PieceModule;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import dev.tecte.chessWar.team.infrastructure.bootstrap.TeamModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * 플러그인의 최상위 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 */
@RequiredArgsConstructor
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

        install(new ExceptionHandlerModule());
        install(new PersistenceModule());
        install(new ListenerModule());
        install(new PieceModule());
        install(new TeamModule());
        install(new BoardModule());
        install(new GameModule());
        install(new CommandModule());
    }
}
