package dev.tecte.chessWar.infrastructure.config;

import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntPredicate;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ConfigResolver {
    private final FileConfiguration defaultConfig;
    private final YmlFileManager userConfigSource;

    public int resolveInt(@NonNull String path, int fallback, @NonNull IntPredicate validator) {
        return tryResolveIntFrom(userConfigSource.getConfig(), path, validator)
                .orElseGet(() -> tryResolveIntFrom(defaultConfig, path, validator)
                        .orElse(fallback));
    }

    @NonNull
    public <T> T resolve(@NonNull String path, @NonNull T fallback, @NonNull Function<String, T> parser) {
        return tryResolveFrom(userConfigSource.getConfig(), path, parser)
                .orElseGet(() -> tryResolveFrom(defaultConfig, path, parser)
                        .orElse(fallback));
    }

    @NonNull
    private OptionalInt tryResolveIntFrom(
            @NonNull ConfigurationSection config,
            @NonNull String path,
            @NonNull IntPredicate validator
    ) {
        if (config.isSet(path)) {
            int value = config.getInt(path);

            if (validator.test(value)) {
                return OptionalInt.of(value);
            }
        }

        return OptionalInt.empty();
    }

    @NonNull
    private <T> Optional<T> tryResolveFrom(
            @NonNull ConfigurationSection config,
            @NonNull String path,
            @NonNull Function<String, T> parser
    ) {
        return Optional.ofNullable(config.getString(path))
                .filter(s -> !s.isEmpty())
                .flatMap(s -> Optional.ofNullable(parser.apply(s)));
    }
}
