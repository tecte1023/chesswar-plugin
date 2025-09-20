package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

@Singleton
public class YmlParser {
    @NonNull
    public ConfigurationSection requireSection(@NonNull ConfigurationSection parent, @NonNull String key) {
        return Optional.ofNullable(parent.getConfigurationSection(key))
                .orElseThrow(() -> new YmlMappingException("Missing required section: '" + key + "' in '" + parent.getCurrentPath() + "'."));
    }

    @NonNull
    public <T> T requireValue(@NonNull ConfigurationSection section, @NonNull String key, @NonNull Class<T> type) {
        if (!section.contains(key)) {
            throw new YmlMappingException("Missing required value: '" + key + "' in '" + section.getCurrentPath() + "'.");
        }

        T value = section.getObject(key, type);

        if (value == null) {
            Object rawValue = section.get(key);
            String actualType = rawValue != null ? rawValue.getClass().getSimpleName() : "null";

            throw new YmlMappingException("Invalid type for '" + key + "' in '" + section.getCurrentPath()
                    + "'. Expected " + type.getSimpleName() + ", but found " + actualType + ".");
        }

        return value;
    }

    @NonNull
    public <T extends Enum<T>> T requireEnum(@NonNull ConfigurationSection section, @NonNull String key, @NonNull Class<T> enumType) {
        String enumName = requireValue(section, key, String.class);

        try {
            return Enum.valueOf(enumType, enumName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new YmlMappingException("Invalid '" + key + "' value in '" + section.getCurrentPath() + "': " + enumName);
        }
    }
}
