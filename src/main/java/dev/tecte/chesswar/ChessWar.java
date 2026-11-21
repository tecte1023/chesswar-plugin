package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.board.BoardCommand;
import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.game.GameCommand;
import dev.tecte.chesswar.game.GameLifecycle;
import dev.tecte.chesswar.game.GameView;
import dev.tecte.chesswar.game.TeamListener;
import dev.tecte.chesswar.game.TeamRegistry;
import dev.tecte.chesswar.piece.PieceLifecycle;
import dev.tecte.chesswar.piece.bukkit.PieceCommand;
import dev.tecte.chesswar.piece.bukkit.PieceListener;
import dev.tecte.chesswar.piece.bukkit.PieceView;
import dev.tecte.chesswar.piece.bukkit.PlayerSessionListener;
import dev.tecte.chesswar.piece.bukkit.TurnBorderView;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChessWar extends JavaPlugin {
    private PieceView pieceView;
    private TurnBorderView turnBorderView;

    @Override
    public void onEnable() {
        var board = new BoardContext();
        var eventBus = new SimulationEventBus();
        var pieceLifecycle = PieceLifecycle.create(eventBus);
        var gameLifecycle = GameLifecycle.create(eventBus);
        var teamRegistry = new TeamRegistry();
        pieceView = PieceView.create(board, eventBus);
        turnBorderView = TurnBorderView.create(board, eventBus);
        GameView.create(gameLifecycle, eventBus);

        var commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new BoardCommand(board));
        commandManager.registerCommand(new PieceCommand(board, pieceLifecycle));
        commandManager.registerCommand(new GameCommand(board, pieceLifecycle, gameLifecycle, teamRegistry));

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PieceListener(board, pieceLifecycle, gameLifecycle), this);
        pluginManager.registerEvents(new TeamListener(gameLifecycle, teamRegistry), this);
        pluginManager.registerEvents(
                new PlayerSessionListener(pieceLifecycle, gameLifecycle, pieceView, turnBorderView),
                this
        );

        getServer().getScheduler().runTaskTimer(this, gameLifecycle::tick, 1L, 1L);
    }

    @Override
    public void onDisable() {
        if (turnBorderView != null) {
            turnBorderView.cleanup();
        }

        if (pieceView != null) {
            pieceView.cleanup();
        }
    }
}

