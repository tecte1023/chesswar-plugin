package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.admin.AdminCommand;
import dev.tecte.chesswar.board.BoardBlockListener;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardVisualGuideListener;
import dev.tecte.chesswar.board.BoardVisualManager;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.admin.GameCommand;
import dev.tecte.chesswar.game.admin.StatsCommand;
import dev.tecte.chesswar.game.listener.CombatListener;
import dev.tecte.chesswar.game.listener.GameEntityListener;
import dev.tecte.chesswar.game.listener.GameReadyListener;
import dev.tecte.chesswar.game.manager.CombatManager;
import dev.tecte.chesswar.game.manager.EnvironmentManager;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.ScoreboardManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import dev.tecte.chesswar.piece.PieceCommand;
import dev.tecte.chesswar.piece.PieceDamageListener;
import dev.tecte.chesswar.piece.PieceEntityLifecycleListener;
import dev.tecte.chesswar.piece.PieceInteractListener;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceSelectionListener;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Accessors(fluent = true)
@Slf4j(topic = "ChessWar")
public class ChessWar extends JavaPlugin {
    private BoardManager boardManager;
    private PieceManager pieceManager;
    private EnvironmentManager environmentManager;
    private GameManager gameManager;
    private MoveValidator moveValidator;
    private BoardVisualManager boardVisualManager;
    private TimerManager timerManager;
    private CombatManager combatManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        initializeManagers();
        pieceManager.setupMythicMobs(this);
        registerListeners();
        registerCommands();
        log.info("ChessWar has been enabled!");
    }

    @Override
    public void onDisable() {
        log.info("ChessWar has been disabled!");
    }

    private void initializeManagers() {
        boardManager = new BoardManager(this);
        pieceManager = new PieceManager(this);
        environmentManager = new EnvironmentManager(this);
        gameManager = new GameManager(this);
        moveValidator = new MoveValidator(gameManager, pieceManager);
        boardVisualManager = new BoardVisualManager(this);
        timerManager = new TimerManager(this);
        combatManager = new CombatManager(this);
        scoreboardManager = new ScoreboardManager(this);
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(boardManager, this);
        pluginManager.registerEvents(pieceManager, this);
        pluginManager.registerEvents(new PieceEntityLifecycleListener(pieceManager), this);
        pluginManager.registerEvents(new GameEntityListener(pieceManager), this);
        pluginManager.registerEvents(new PieceSelectionListener(this, gameManager), this);
        pluginManager.registerEvents(new GameReadyListener(this, gameManager, boardManager), this);
        pluginManager.registerEvents(new BoardBlockListener(gameManager, boardManager), this);
        pluginManager.registerEvents(new PieceInteractListener(gameManager, combatManager), this);
        pluginManager.registerEvents(new PieceDamageListener(combatManager), this);
        pluginManager.registerEvents(new BoardVisualGuideListener(boardVisualManager), this);
        pluginManager.registerEvents(new CombatListener(this), this);
        pluginManager.registerEvents(timerManager, this);
        pluginManager.registerEvents(scoreboardManager, this);
        pluginManager.registerEvents(environmentManager, this);
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.registerCommand(new GameCommand(
                this,
                gameManager,
                boardManager,
                timerManager
        ));
        commandManager.registerCommand(new PieceCommand(gameManager, timerManager));
        commandManager.registerCommand(new StatsCommand(gameManager));
        commandManager.registerCommand(new AdminCommand(
                this,
                gameManager,
                boardManager,
                pieceManager,
                moveValidator,
                combatManager,
                environmentManager
        ));
    }
}
