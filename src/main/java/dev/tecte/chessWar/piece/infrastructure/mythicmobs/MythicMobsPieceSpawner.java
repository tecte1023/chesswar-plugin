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
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * MythicMobs 플러그인을 사용하여 실제 엔티티를 스폰하는 {@link PieceSpawner}의 구현체입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MythicMobsPieceSpawner implements PieceSpawner {
    private static final int MOB_LEVEL = 1;

    private final MobManager mobManager;
    private final PieceIdResolver pieceIdResolver;

    /**
     * 지정된 위치에 기물 명세에 해당하는 MythicMob을 스폰합니다.
     *
     * @param spec     스폰할 기물의 명세
     * @param location 스폰될 위치
     * @return 스폰된 Bukkit 엔티티
     * @throws MythicMobSpawnException 해당 ID의 MythicMob을 찾을 수 없거나 스폰에 실패한 경우
     */
    @NonNull
    @Override
    public Entity spawnPiece(@NonNull PieceSpec spec, @NonNull Location location) {
        String templateId = pieceIdResolver.resolveId(spec.teamColor(), spec.type());
        MythicMob mythicMob = mobManager.getMythicMob(templateId)
                .orElseThrow(() -> MythicMobSpawnException.notFound(templateId));
        ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), MOB_LEVEL);

        if (activeMob == null) {
            throw MythicMobSpawnException.spawnFailed(templateId);
        }

        return activeMob.getEntity().getBukkitEntity();
    }
}
