package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.admin.BoardAdminCommand;
import dev.tecte.chesswar.admin.GameAdminCommand;
import dev.tecte.chesswar.admin.TeamAdminCommand;
import dev.tecte.chesswar.board.Board;
import dev.tecte.chesswar.board.BoardComponent;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardPresenter;
import dev.tecte.chesswar.board.BoardUIComponent;
import dev.tecte.chesswar.game.GameLifecycleManager;
import dev.tecte.chesswar.game.GameLifecyclePresenter;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.GamePhaseComponent;
import dev.tecte.chesswar.game.InternalEventBus;
import dev.tecte.chesswar.game.StartTriggerUIComponent;
import dev.tecte.chesswar.game.StartingPhaseManager;
import dev.tecte.chesswar.game.StartingPhasePresenter;
import dev.tecte.chesswar.game.WaitingPhaseListener;
import dev.tecte.chesswar.game.WaitingPhaseManager;
import dev.tecte.chesswar.game.WaitingPhasePresenter;
import dev.tecte.chesswar.team.TeamManager;
import dev.tecte.chesswar.team.TeamPresenter;
import dev.tecte.chesswar.team.TeamRosterComponent;
import dev.tecte.chesswar.team.TeamSelectionListener;
import dev.tecte.chesswar.team.TeamSide;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j(topic = "ChessWar")
public final class ChessWar extends JavaPlugin {
    @Override
    public void onEnable() {
        final var commandManager = new PaperCommandManager(this);
        final PluginManager pluginManager = getServer().getPluginManager();
        final var internalEventBus = new InternalEventBus();

        final var boardComponent = new BoardComponent(Board.create(getDefaultSpawnLocation()));
        final var boardUIComponent = new BoardUIComponent(new ArrayList<>());
        final var gamePhaseComponent = new GamePhaseComponent(GamePhase.WAITING);
        final var teamRosterComponent = new TeamRosterComponent(new UUID[TeamSide.values().length][0]);
        final var startTriggerUIComponent = new StartTriggerUIComponent(null, null);

        final var boardPresenter = new BoardPresenter(this);
        final var teamPresenter = new TeamPresenter();
        final var waitingPhasePresenter = new WaitingPhasePresenter();
        final var startingPhasePresenter = new StartingPhasePresenter();
        final var gameLifecyclePresenter = new GameLifecyclePresenter();

        final var boardManager = new BoardManager(boardComponent, boardUIComponent, boardPresenter);
        final var teamManager = new TeamManager(teamRosterComponent, gamePhaseComponent);
        final var startingPhaseManager = new StartingPhaseManager(
                this,
                gamePhaseComponent,
                teamRosterComponent,
                boardComponent,
                startingPhasePresenter
        );
        final var waitingPhaseManager = new WaitingPhaseManager(
                startTriggerUIComponent,
                gamePhaseComponent,
                waitingPhasePresenter,
                internalEventBus
        );
        final var gameLifecycleManager = new GameLifecycleManager(
                gamePhaseComponent,
                teamRosterComponent,
                gameLifecyclePresenter,
                internalEventBus
        );

        final var teamSelectionListener = new TeamSelectionListener(teamManager, teamPresenter);
        final var waitingPhaseListener = new WaitingPhaseListener(waitingPhaseManager);

        internalEventBus.registerPhaseListener(waitingPhaseManager);
        internalEventBus.registerPhaseListener(startingPhaseManager);

        pluginManager.registerEvents(teamSelectionListener, this);
        pluginManager.registerEvents(waitingPhaseListener, this);

        commandManager.registerCommand(new BoardAdminCommand(boardManager));
        commandManager.registerCommand(new TeamAdminCommand(teamManager, teamPresenter));
        commandManager.registerCommand(new GameAdminCommand(waitingPhaseManager, gameLifecycleManager));

        log.info("ChessWar has been enabled!");
    }

    @Override
    public void onDisable() {
        log.info("ChessWar has been disabled!");
    }

    @NotNull
    private Location getDefaultSpawnLocation() {
        final Location location = Bukkit.getWorlds().getFirst().getSpawnLocation();

        location.setYaw(90f);

        return location;
    }
}
