package dev.tecte.chessWar.port;

import lombok.NonNull;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * 월드 내 물리 엔티티를 식별하고 찾습니다.
 */
public interface EntityResolver {
    /**
     * 엔티티를 찾습니다.
     *
     * @param entityId 엔티티 ID
     * @return 찾은 엔티티
     */
    @NonNull
    Optional<Entity> findEntity(@NonNull UUID entityId);
}
