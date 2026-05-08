package dev.tecte.chessWar.piece.infrastructure.mythicmobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 기물 구동을 위한 MythicMobs 인프라를 설정합니다.
 */
@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor
public class MythicMobsSetup {
    private static final String PIECE_DEFINITION_FILENAME = "Piece.yml";

    private final JavaPlugin plugin;
    private final PluginManager pluginManager;

    /**
     * 기물 정의 파일을 MythicMobs 엔진에 동기화합니다.
     */
    public void run() {
        Plugin mythicMobsPlugin = pluginManager.getPlugin("MythicMobs");

        if (mythicMobsPlugin == null) {
            return;
        }

        File targetFile = new File(new File(mythicMobsPlugin.getDataFolder(), "Mobs"), PIECE_DEFINITION_FILENAME);

        if (targetFile.exists()) {
            return;
        }

        File targetDir = targetFile.getParentFile();

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            log.atError().log("Failed to create MythicMobs Mobs directory. The plugin may not work as expected.");

            return;
        }

        String resourcePath = "mobs/" + PIECE_DEFINITION_FILENAME;

        try (InputStream inputStream = plugin.getResource(resourcePath)) {
            if (inputStream == null) {
                log.atError().log("Could not find resource: {}. The plugin may not work as expected.", resourcePath);

                return;
            }

            Files.copy(inputStream, targetFile.toPath());
            log.atInfo().log("Successfully copied {} to MythicMobs folder.", resourcePath);
        } catch (IOException e) {
            log.atError().setCause(e).log("Failed to copy {} to MythicMobs folder.", resourcePath);
        }
    }
}
