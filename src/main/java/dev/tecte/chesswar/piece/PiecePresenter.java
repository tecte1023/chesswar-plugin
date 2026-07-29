package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.team.TeamSide;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Slf4j(topic = "ChessWar")
public final class PiecePresenter {
    private final Server server = Bukkit.getServer();
    private final MobManager mobManager = MythicProvider.get().getMobManager();
    private final String[][] mobIdCache = {
            {"WhiteKing", "WhiteQueen", "WhiteRook", "WhiteBishop", "WhiteKnight", "WhitePawn"},
            {"BlackKing", "BlackQueen", "BlackRook", "BlackBishop", "BlackKnight", "BlackPawn"}
    };

    @Nullable
    public UUID showPiece(
            @NotNull final TeamSide teamSide,
            @NotNull final PieceType type,
            @NotNull final Location location
    ) {
        final String mobId = mobIdCache[teamSide.ordinal()][type.ordinal()];
        final MythicMob mythicMob = mobManager.getMythicMob(mobId).orElse(null);

        if (mythicMob == null) {
            printConfigMissingError(mobId);
            return null;
        }

        final ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), 1);

        if (activeMob == null) {
            printSpawnFailedError(mobId, location);
            return null;
        }

        return activeMob.getEntity().getBukkitEntity().getUniqueId();
    }

    private void printConfigMissingError(final String mobId) {
        log.error(
                """
                ========================================
                🚨 기물 소환 실패: '{}'
                
                원인: 해당 ID의 몹 설정을 찾을 수 없습니다.
                해결: MythicMobs 플러그인의 설정 파일(YAML)을 확인해주세요.
                ========================================
                """,
                mobId
        );
    }

    private void printSpawnFailedError(final String mobId, final Location location) {
        log.error(
                """
                ========================================
                🚨 기물 스폰 실패: '{}'
                
                위치: {}
                원인: 물리 엔진 에러로 몹이 소환되지 못했습니다.
                해결: 청크 로딩 상태나 타 플러그인 충돌을 확인해주세요.
                ========================================
                """,
                mobId,
                location
        );
    }

    public void hidePiece(@NotNull final UUID entityId) {
        final Entity entity = server.getEntity(entityId);

        if (entity == null) {
            return;
        }

        entity.remove();
    }
}
