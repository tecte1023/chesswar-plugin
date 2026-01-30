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
 * MythicMobs 연동을 설정합니다.
 * 플러그인 활성화 시, 기물에 대한 MythicMobs 설정 파일이 없으면 자동으로 복사합니다.
 */
@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor
public class MythicMobsSetup {
    private static final String MYTHIC_MOBS_PLUGIN_NAME = "MythicMobs";
    private static final String MYTHIC_MOBS_TARGET_FOLDER_NAME = "Mobs";
    private static final String PIECE_DEFINITION_FILENAME = "Piece.yml";
    private static final String PIECE_DEFINITION_RESOURCE_PATH = "mobs/" + PIECE_DEFINITION_FILENAME;

    private final PluginManager pluginManager;
    private final JavaPlugin plugin;

    /**
     * MythicMobs 플러그인이 설치되어 있는지 확인하고, 필요한 설정 파일을 복사합니다.
     * 만약 `Piece.yml` 파일이 MythicMobs의 `Mobs` 폴더에 이미 존재하면 아무 작업도 수행하지 않습니다.
     */
    public void run() {
        Plugin mythicMobsPlugin = pluginManager.getPlugin(MYTHIC_MOBS_PLUGIN_NAME);

        if (mythicMobsPlugin == null) {
            return;
        }

        File targetFile = new File(
                new File(mythicMobsPlugin.getDataFolder(), MYTHIC_MOBS_TARGET_FOLDER_NAME),
                PIECE_DEFINITION_FILENAME
        );

        if (targetFile.exists()) {
            return;
        }

        File targetDir = targetFile.getParentFile();

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            log.error("Failed to create MythicMobs Mobs directory. The plugin may not work as expected.");

            return;
        }

        try (InputStream inputStream = plugin.getResource(PIECE_DEFINITION_RESOURCE_PATH)) {
            if (inputStream == null) {
                log.error("Could not find resource: {}. The plugin may not work as expected.",
                        PIECE_DEFINITION_RESOURCE_PATH);

                return;
            }

            Files.copy(inputStream, targetFile.toPath());
            log.info("Successfully copied {} to MythicMobs folder.", PIECE_DEFINITION_RESOURCE_PATH);
        } catch (IOException e) {
            log.error("Failed to copy {} to MythicMobs folder.", PIECE_DEFINITION_RESOURCE_PATH, e);
        }
    }
}
