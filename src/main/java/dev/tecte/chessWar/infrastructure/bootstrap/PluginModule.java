package dev.tecte.chessWar.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.infrastructure.bootstrap.BoardModule;
import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.game.infrastructure.bootstrap.GameModule;
import dev.tecte.chessWar.infrastructure.bukkit.BukkitEntityResolver;
import dev.tecte.chessWar.infrastructure.bukkit.BukkitEventDispatcher;
import dev.tecte.chessWar.infrastructure.bukkit.BukkitTaskRunner;
import dev.tecte.chessWar.infrastructure.bukkit.BukkitUserNotifier;
import dev.tecte.chessWar.infrastructure.bukkit.BukkitUserResolver;
import dev.tecte.chessWar.infrastructure.bukkit.BukkitWorldResolver;
import dev.tecte.chessWar.infrastructure.command.MainCommand;
import dev.tecte.chessWar.piece.infrastructure.bootstrap.PieceModule;
import dev.tecte.chessWar.port.EntityResolver;
import dev.tecte.chessWar.port.TaskRunner;
import dev.tecte.chessWar.port.UserNotifier;
import dev.tecte.chessWar.port.UserResolver;
import dev.tecte.chessWar.port.WorldResolver;
import dev.tecte.chessWar.team.infrastructure.bootstrap.TeamModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * 플러그인의 최상위 의존성을 설정합니다.
 */
@RequiredArgsConstructor
public class PluginModule extends AbstractModule {
    private final ChessWar plugin;

    @Override
    protected void configure() {
        Server server = plugin.getServer();

        bind(ChessWar.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);
        bind(DomainEventDispatcher.class).to(BukkitEventDispatcher.class);
        bind(TaskRunner.class).to(BukkitTaskRunner.class);
        bind(UserNotifier.class).to(BukkitUserNotifier.class);
        bind(UserResolver.class).to(BukkitUserResolver.class);
        bind(WorldResolver.class).to(BukkitWorldResolver.class);
        bind(EntityResolver.class).to(BukkitEntityResolver.class);
        bind(ConsoleCommandSender.class).toInstance(Bukkit.getConsoleSender());
        bind(BukkitScheduler.class).toInstance(server.getScheduler());
        bind(PluginManager.class).toInstance(server.getPluginManager());

        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(MainCommand.class);

        install(new ExceptionHandlerModule());
        install(new PersistenceModule());
        install(new PieceModule());
        install(new TeamModule());
        install(new BoardModule());
        install(new GameModule());
        install(new ListenerModule());
        install(new CommandModule());
    }
}
