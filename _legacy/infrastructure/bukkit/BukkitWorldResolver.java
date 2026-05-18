package dev.tecte.chessWar.infrastructure.bukkit;

import dev.tecte.chessWar.port.WorldResolver;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.function.Function;

/**
 * Bukkit 기반으로 월드를 찾습니다.
 */
@Singleton
public class BukkitWorldResolver implements WorldResolver {
    @Override
    public <E extends RuntimeException> void ensureExists(
            @NonNull String name,
            @NonNull Function<String, E> onFailure
    ) {
        resolve(name, onFailure);
    }

    @NonNull
    @Override
    public <E extends RuntimeException> World resolve(
            @NonNull String name,
            @NonNull Function<String, E> onFailure
    ) {
        World world = Bukkit.getWorld(name);

        if (world == null) {
            throw onFailure.apply(name);
        }

        return world;
    }
}
