package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.admin.AdminCommand;
import dev.tecte.chesswar.board.BoardBlockListener;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardVisualGuideListener;
import dev.tecte.chesswar.board.BoardVisualManager;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.CombatManager;
import dev.tecte.chesswar.game.EnvironmentManager;
import dev.tecte.chesswar.game.GameCommand;
import dev.tecte.chesswar.game.GameEntityListener;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GameReadyListener;
import dev.tecte.chesswar.game.ScoreboardManager;
import dev.tecte.chesswar.game.StatsCommand;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.piece.PieceCommand;
import dev.tecte.chesswar.piece.PieceDamageListener;
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
        boardManager = new BoardManager();
        pieceManager = new PieceManager();
        environmentManager = new EnvironmentManager();
        gameManager = new GameManager();
        moveValidator = new MoveValidator(gameManager, pieceManager);
        boardVisualManager = new BoardVisualManager(gameManager, boardManager, pieceManager, moveValidator);
        timerManager = new TimerManager(this, gameManager, pieceManager);
        combatManager = new CombatManager(
                this,
                gameManager,
                boardManager,
                pieceManager,
                moveValidator,
                timerManager
        );
        scoreboardManager = new ScoreboardManager(this, gameManager, timerManager);
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new PieceSelectionListener(this, gameManager), this);
        pluginManager.registerEvents(
                new GameReadyListener(this, gameManager, boardManager, timerManager),
                this
        );
        pluginManager.registerEvents(new PieceInteractListener(gameManager, combatManager), this);
        pluginManager.registerEvents(new PieceDamageListener(combatManager), this);
        pluginManager.registerEvents(new BoardVisualGuideListener(boardVisualManager), this);
        pluginManager.registerEvents(new BoardBlockListener(gameManager, boardManager), this);
        pluginManager.registerEvents(new GameEntityListener(gameManager, pieceManager), this);
        pluginManager.registerEvents(timerManager, this);
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.registerCommand(new GameCommand(
                this,
                gameManager,
                boardManager,
                pieceManager,
                timerManager
        ));
        commandManager.registerCommand(new PieceCommand(gameManager, pieceManager, timerManager));
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
