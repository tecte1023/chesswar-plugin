package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.admin.command.BoardAdminCommand;
import dev.tecte.chesswar.board.Board;
import dev.tecte.chesswar.board.BoardComponent;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardUIComponent;
import dev.tecte.chesswar.board.BoardPresenter;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.GamePhaseComponent;
import dev.tecte.chesswar.team.TeamManager;
import dev.tecte.chesswar.team.TeamPresenter;
import dev.tecte.chesswar.team.TeamRosterComponent;
import dev.tecte.chesswar.team.TeamSelectionListener;
import dev.tecte.chesswar.team.TeamSide;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j(topic = "ChessWar")
public final class ChessWar extends JavaPlugin {
    @Override
    public void onEnable() {
        final Location spawnLocation = Bukkit.getWorlds().getFirst().getSpawnLocation();
        final var commandManager = new PaperCommandManager(this);

        spawnLocation.setYaw(90f);

        final var boardComponent = new BoardComponent(Board.create(spawnLocation));
        final var boardUIComponent = new BoardUIComponent(new ArrayList<>());
        final var boardPresenter = new BoardPresenter(this);
        final var boardManager = new BoardManager(boardComponent, boardUIComponent, boardPresenter);

        commandManager.registerCommand(new BoardAdminCommand(boardManager));

        final var gamePhaseComponent = new GamePhaseComponent(GamePhase.WAITING);
        final var teamRosterComponent = new TeamRosterComponent(new UUID[TeamSide.values().length][0]);
        final var teamPresenter = new TeamPresenter();
        final var teamManager = new TeamManager(teamRosterComponent, gamePhaseComponent, teamPresenter);
        final var teamSelectionListener = new TeamSelectionListener(teamManager);

        getServer().getPluginManager().registerEvents(teamSelectionListener, this);

        log.info("ChessWar has been enabled!");
    }

    @Override
    public void onDisable() {
        log.info("ChessWar has been disabled!");
    }
}
