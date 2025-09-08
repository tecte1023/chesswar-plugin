package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public abstract class AbstractYmlRepository<K, V> implements PersistableState {
    private final JavaPlugin plugin;
    private final YmlFileManager fileManager;
    private final YmlMapper<K, V> mapper;
    private final Logger logger;

    private final Map<K, V> cache = new ConcurrentHashMap<>();

    @NonNull
    protected abstract String getDataPath();

    @NonNull
    protected abstract K getKey(@NonNull V entity);

    @NonNull
    protected abstract K convertKey(@NonNull String keyString);

    @Override
    public void loadAll() {
        ConfigurationSection section = fileManager.getConfig().getConfigurationSection(getDataPath());

        if (section == null) {
            return;
        }

        for (String keyString : section.getKeys(false)) {
            try {
                ConfigurationSection entitySection = section.getConfigurationSection(keyString);

                if (entitySection == null) {
                    continue;
                }

                K key = convertKey(keyString);
                V entity = mapper.fromMap(key, entitySection.getValues(false));

                if (entity != null) {
                    cache.put(key, entity);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to deserialize entity " + keyString + " in " + getDataPath(), e);
            }
        }
    }

    @Override
    public void persistCache() {
        final String dataPath = getDataPath();

        fileManager.set(dataPath, null);

        for (Map.Entry<K, V> entry : cache.entrySet()) {
            final String path = dataPath + "." + entry.getKey().toString();

            fileManager.set(path, mapper.toMap(entry.getValue()));
        }

        fileManager.save();
    }

    public void save(@NonNull V entity) {
        K key = getKey(entity);

        cache.put(key, entity);
        persistChangeAsync(key.toString(), mapper.toMap(entity));
    }

    private void persistChangeAsync(@NonNull String key, Object value) {
        final String path = getDataPath() + "." + key;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            fileManager.set(path, value);
            fileManager.save();
        });
    }
}
