package dev.tecte.chessWar.infrastructure.bukkit;

import dev.tecte.chessWar.port.EntityResolver;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * Bukkit 기반으로 엔티티를 찾습니다.
 */
@Singleton
public class BukkitEntityResolver implements EntityResolver {
    @NonNull
    @Override
    public Optional<Entity> findEntity(@NonNull UUID entityId) {
        return Optional.ofNullable(Bukkit.getEntity(entityId));
    }
}
