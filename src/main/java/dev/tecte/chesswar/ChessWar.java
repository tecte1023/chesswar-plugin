package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.command.ChessBoardCommand;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.ScoreboardManager;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.listener.ChessBlockListener;
import dev.tecte.chesswar.listener.ChessDamageListener;
import dev.tecte.chesswar.listener.ChessInteractListener;
import dev.tecte.chesswar.listener.ChessPieceSelectionListener;
import dev.tecte.chesswar.listener.ChessReadyListener;
import dev.tecte.chesswar.listener.ChessVisualGuideListener;
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
    private GameManager gameManager;
    private TimerManager timerManager;
    private ScoreboardManager scoreboardManager;
    private MoveValidator moveValidator;

    @Override
    public void onEnable() {
        boardManager = new BoardManager();
        gameManager = new GameManager();
        timerManager = new TimerManager(this, gameManager);
        scoreboardManager = new ScoreboardManager(gameManager, timerManager);
        moveValidator = new MoveValidator(gameManager);

        PaperCommandManager commandManager = new PaperCommandManager(this);
        PluginManager pluginManager = getServer().getPluginManager();

        timerManager.scoreboardManager(scoreboardManager);
        commandManager.registerCommand(new ChessBoardCommand(
                this,
                gameManager,
                boardManager,
                timerManager,
                moveValidator
        ));
        pluginManager.registerEvents(
                new ChessInteractListener(gameManager, boardManager, moveValidator),
                this
        );
        pluginManager.registerEvents(
                new ChessDamageListener(gameManager, boardManager, moveValidator, timerManager),
                this
        );
        pluginManager.registerEvents(
                new ChessVisualGuideListener(gameManager, boardManager, moveValidator),
                this
        );
        pluginManager.registerEvents(
                new ChessPieceSelectionListener(this, gameManager),
                this
        );
        pluginManager.registerEvents(new ChessReadyListener(this, gameManager), this);
        pluginManager.registerEvents(new ChessBlockListener(gameManager), this);
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
