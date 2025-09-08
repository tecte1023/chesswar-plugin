package dev.tecte.chessWar.infrastructure.file;

import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceInitializationException;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceWriteException;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class YmlFileManager {
    private final File file;
    @Getter
    private final FileConfiguration config;

    @Inject
    public YmlFileManager(@NonNull JavaPlugin plugin, @NonNull String fileName) {
        file = new File(plugin.getDataFolder(), fileName);

        File parentDir = file.getParentFile();

        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new PersistenceInitializationException("Failed to create parent directories for: " + file.getPath());
        }

        if (!file.exists()) {
            try {
                plugin.saveResource(fileName, false);
            } catch (IllegalArgumentException e) {
                try {
                    if (!file.createNewFile()) {
                        plugin.getLogger().warning("File was created by another process between checks: " + fileName);
                    }
                } catch (IOException ex) {
                    throw new PersistenceInitializationException("Could not create new file: " + fileName, ex);
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void set(@NonNull String path, Object value) {
        config.set(path, value);
    }

    public synchronized void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new PersistenceWriteException("Could not save to " + file.getName(), e);
        }
    }
}
