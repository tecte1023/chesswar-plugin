package dev.tecte.chessWar.piece.infrastructure.mythicmobs;

import dev.tecte.chessWar.piece.application.port.PieceIdResolver;
import dev.tecte.chessWar.piece.application.port.PieceSpawner;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.exception.MythicMobSpawnException;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * MythicMobs를 사용하여 기물을 소환하고 제거합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MythicMobsPieceSpawner implements PieceSpawner {
    private static final int MOB_LEVEL = 1;

    private final MobManager mobManager;
    private final PieceIdResolver pieceIdResolver;

    @NonNull
    @Override
    public Entity spawn(@NonNull PieceSpec spec, @NonNull Location location) {
        String templateId = pieceIdResolver.resolveId(spec.teamColor(), spec.type());
        MythicMob mythicMob = mobManager.getMythicMob(templateId)
                .orElseThrow(() -> MythicMobSpawnException.notFound(templateId));
        ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), MOB_LEVEL);

        if (activeMob == null) {
            throw MythicMobSpawnException.spawnFailed(templateId);
        }

        return activeMob.getEntity().getBukkitEntity();
    }

    @Override
    public void despawn(@NonNull UUID entityId) {
        Entity entity = Bukkit.getEntity(entityId);

        if (entity != null) {
            entity.remove();
        }
    }
}
