package dev.tecte.chessWar.piece.application.port;

import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * 체스 기물을 소환하거나 제거합니다.
 */
public interface PieceSpawner {
    /**
     * 지정된 위치에 기물을 소환합니다.
     *
     * @param spec     소환할 기물의 명세
     * @param location 소환할 위치
     * @return 소환된 엔티티
     */
    @NonNull
    Entity spawn(@NonNull PieceSpec spec, @NonNull Location location);

    /**
     * 식별자에 해당하는 기물 엔티티를 제거합니다.
     *
     * @param entityId 제거할 엔티티의 식별자 UUID
     */
    void despawn(@NonNull UUID entityId);
}
