package dev.tecte.chesswar.piece;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
public class PieceBootstrap {
    private final Plugin plugin;

    public void setupMythicMobs() {
        final Plugin mythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs");

        if (mythicMobs == null) {
            return;
        }

        final File targetFile = new File(new File(mythicMobs.getDataFolder(), "Mobs"), "Piece.yml");

        if (targetFile.exists()) {
            return;
        }

        final File parentDir = targetFile.getParentFile();

        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            log.error("Failed to create MythicMobs piece directory.");
            return;
        }

        try (final InputStream in = plugin.getResource("mobs/Piece.yml")) {
            if (in != null) {
                Files.copy(in, targetFile.toPath());
                log.info("Successfully synced Piece.yml to MythicMobs folder.");
            }
        } catch (IOException e) {
            log.error("Failed to sync Piece.yml: {}", e.getMessage());
        }
    }
}
