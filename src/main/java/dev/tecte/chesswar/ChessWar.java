package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.board.BoardBlockListener;
import dev.tecte.chesswar.board.BoardCommand;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardVisualGuideListener;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.GameEntityListener;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GameReadyListener;
import dev.tecte.chesswar.game.ScoreboardManager;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.piece.PieceDamageListener;
import dev.tecte.chesswar.piece.PieceInteractListener;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceSelectionListener;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Getter
@Accessors(fluent = true)
@Slf4j(topic = "ChessWar")
public class ChessWar extends JavaPlugin {
    private BoardManager boardManager;
    private PieceManager pieceManager;
    private GameManager gameManager;
    private TimerManager timerManager;
    private ScoreboardManager scoreboardManager;
    private MoveValidator moveValidator;

    @Override
    public void onEnable() {
        boardManager = new BoardManager();
        pieceManager = new PieceManager();
        gameManager = new GameManager();
        timerManager = new TimerManager(this, gameManager, pieceManager);
        scoreboardManager = new ScoreboardManager(gameManager, timerManager);
        moveValidator = new MoveValidator(gameManager, pieceManager);

        PaperCommandManager commandManager = new PaperCommandManager(this);
        PluginManager pluginManager = getServer().getPluginManager();

        timerManager.scoreboardManager(scoreboardManager);
        commandManager.registerCommand(new BoardCommand(
                this,
                gameManager,
                boardManager,
                pieceManager,
                timerManager,
                moveValidator
        ));
        pluginManager.registerEvents(
                new PieceInteractListener(gameManager, boardManager, pieceManager, moveValidator),
                this
        );
        pluginManager.registerEvents(
                new PieceDamageListener(this, gameManager, boardManager, pieceManager, moveValidator, timerManager),
                this
        );
        pluginManager.registerEvents(
                new BoardVisualGuideListener(gameManager, boardManager, pieceManager, moveValidator),
                this
        );
        pluginManager.registerEvents(
                new GameEntityListener(gameManager, pieceManager),
                this
        );
        pluginManager.registerEvents(
                new PieceSelectionListener(this, gameManager, timerManager),
                this
        );
        pluginManager.registerEvents(new GameReadyListener(this, gameManager, boardManager, timerManager), this);
        pluginManager.registerEvents(new BoardBlockListener(gameManager, boardManager), this);
        pluginManager.registerEvents(timerManager, this);
        setupMythicMobs();
        setupOptimalEngineSettings();
        log.info("ChessWar has been enabled with optimized engine settings!");
    }

    @Override
    public void onDisable() {
        log.info("ChessWar has been disabled!");
    }

    private void setupMythicMobs() {
        Plugin mythicMobs = getServer().getPluginManager().getPlugin("MythicMobs");

        if (mythicMobs == null) {
            return;
        }

        File targetFile = new File(new File(mythicMobs.getDataFolder(), "Mobs"), "Piece.yml");

        if (targetFile.exists()) {
            return;
        }

        File parentDir = targetFile.getParentFile();

        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            log.error("Failed to create MythicMobs piece directory.");
            return;
        }

        try (InputStream in = getResource("mobs/Piece.yml")) {
            if (in != null) {
                Files.copy(in, targetFile.toPath());
                log.info("Successfully synced Piece.yml to MythicMobs folder.");
            }
        } catch (IOException e) {
            log.error("Failed to sync Piece.yml: {}", e.getMessage());
        }
    }

    private void setupOptimalEngineSettings() {
        Bukkit.getWorlds().forEach(world -> {
            setGameRuleIfDifferent(world, GameRule.NATURAL_REGENERATION, false);
            setGameRuleIfDifferent(world, GameRule.DO_DAYLIGHT_CYCLE, false);
            setGameRuleIfDifferent(world, GameRule.DO_WEATHER_CYCLE, false);
            setGameRuleIfDifferent(world, GameRule.DO_MOB_SPAWNING, false);
            setGameRuleIfDifferent(world, GameRule.DO_TRADER_SPAWNING, false);
            setGameRuleIfDifferent(world, GameRule.DO_PATROL_SPAWNING, false);
            setGameRuleIfDifferent(world, GameRule.MOB_GRIEFING, false);
            setGameRuleIfDifferent(world, GameRule.KEEP_INVENTORY, true);
            setGameRuleIfDifferent(world, GameRule.DO_FIRE_TICK, false);
            setGameRuleIfDifferent(world, GameRule.DO_TILE_DROPS, false);
            setGameRuleIfDifferent(world, GameRule.ANNOUNCE_ADVANCEMENTS, false);
            setGameRuleIfDifferent(world, GameRule.SPAWN_RADIUS, 0);

            if (world.getDifficulty() != Difficulty.NORMAL) {
                world.setDifficulty(Difficulty.NORMAL);
            }

            if (!world.getPVP()) {
                world.setPVP(true);
            }
        });
    }

    private <T> void setGameRuleIfDifferent(World world, GameRule<T> rule, T value) {
        T currentValue = world.getGameRuleValue(rule);

        if (currentValue != null && !currentValue.equals(value)) {
            world.setGameRule(rule, value);
        }
    }
}
