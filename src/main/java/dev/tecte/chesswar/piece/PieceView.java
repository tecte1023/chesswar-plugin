package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.core.View;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PieceView implements View {
    private static final String DEFAULT_MOB_ID = "WhitePawn";

    private final BoardContext board;
    private final MobExecutor mobManager = (MobExecutor) MythicProvider.get().getMobManager();
    private final Map<Integer, UUID> visualEntities = new HashMap<>();

    public static PieceView create(final BoardContext board, final SimulationEventBus eventBus) {
        var view = new PieceView(board);
        eventBus.subscribe(PieceSpawnEvent.class, view::onPieceSpawn);
        eventBus.subscribe(PieceClearEvent.class, view::onPieceClear);

        return view;
    }

    public void cleanup() {
        for (UUID visualId : visualEntities.values()) {
            var activeMob = mobManager.getActiveMob(visualId).orElse(null);
            if (activeMob != null) {
                activeMob.despawn();
                continue;
            }

            var bukkitEntity = Bukkit.getEntity(visualId);
            if (bukkitEntity != null) {
                bukkitEntity.remove();
            }
        }

        visualEntities.clear();
    }

    private void onPieceSpawn(final PieceSpawnEvent event) {
        Entity piece = event.piece();
        var gridComponent = piece.component(GridComponent.class);

        var world = Bukkit.getWorld(board.worldId());
        if (world == null) {
            log.error("체스판 월드를 찾을 수 없습니다: {}", board.worldId());
            return;
        }

        Location loc = board.toWorldPos(gridComponent.gridPos()).toLocation(world);
        loc.setYaw(board.yaw());

        var mythicMob = mobManager.getMythicMob(DEFAULT_MOB_ID).orElse(null);
        if (mythicMob == null) {
            log.error("MythicMob 설정을 찾을 수 없습니다: {}", DEFAULT_MOB_ID);
            return;
        }

        ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(loc), 1);
        if (activeMob == null) {
            log.error("MythicMob 스폰에 실패했습니다: {}, 위치: {}", DEFAULT_MOB_ID, loc);
            return;
        }

        visualEntities.put(piece.id(), activeMob.getEntity().getBukkitEntity().getUniqueId());
    }

    private void onPieceClear(final PieceClearEvent event) {
        cleanup();
    }
}
