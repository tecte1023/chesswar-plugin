package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.command.ChessBoardCommand;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.ScoreboardManager;
import dev.tecte.chesswar.game.TimerManager;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Accessors(fluent = true)
@Slf4j(topic = "ChessWar")
public class ChessWar extends JavaPlugin {
    private BoardManager boardManager;
    private GameManager gameManager;
    private TimerManager timerManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        boardManager = new BoardManager();
        gameManager = new GameManager();
        timerManager = new TimerManager(this, gameManager);
        scoreboardManager = new ScoreboardManager(gameManager, timerManager);

        PaperCommandManager commandManager = new PaperCommandManager(this);

        timerManager.scoreboardManager(scoreboardManager);
        commandManager.registerCommand(new ChessBoardCommand(
                this,
                gameManager,
                boardManager,
                timerManager
        ));
        log.info("ChessWar has been enabled!");
    }

    @Override
    public void onDisable() {
        log.info("ChessWar has been disabled!");
    }
}
