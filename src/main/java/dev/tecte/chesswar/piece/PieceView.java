package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.core.View;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
        eventBus.subscribe(PieceBindEvent.class, view::onPieceBind);
        eventBus.subscribe(PieceMoveEvent.class, view::onPieceMove);
        eventBus.subscribe(PieceClearEvent.class, view::onPieceClear);

        return view;
    }

    public void cleanup() {
        for (UUID visualId : visualEntities.values()) {
            despawnVisual(visualId);
        }

        visualEntities.clear();
    }

    private void onPieceSpawn(final PieceSpawnEvent event) {
        World world = findWorld();
        if (world == null) {
            return;
        }

        Entity piece = event.piece();
        var gridComponent = piece.component(GridComponent.class);
        Location loc = toTileLocation(gridComponent, world);

        MythicMob mythicMob = mobManager.getMythicMob(DEFAULT_MOB_ID).orElse(null);
        if (mythicMob == null) {
            log.error("기물 외형 MythicMob 설정을 찾을 수 없습니다: pieceId={}, mobId={}", piece.id(), DEFAULT_MOB_ID);
            return;
        }

        ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(loc), 1);
        if (activeMob == null) {
            log.error("기물 외형 MythicMob 스폰에 실패했습니다: pieceId={}, mobId={}, loc={}", piece.id(), DEFAULT_MOB_ID, loc);
            return;
        }

        visualEntities.put(piece.id(), activeMob.getEntity().getBukkitEntity().getUniqueId());
    }

    private void onPieceBind(final PieceBindEvent event) {
        World world = findWorld();
        if (world == null) {
            return;
        }

        Entity piece = event.piece();
        UUID playerId = piece.component(PlayerComponent.class).playerId();
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            log.warn("배정된 플레이어가 오프라인 상태여서 텔레포트할 수 없습니다: pieceId={}, playerId={}", piece.id(), playerId);
            return;
        }

        var gridComponent = piece.component(GridComponent.class);
        Location loc = toTileLocation(gridComponent, world);

        player.teleport(loc);
        UUID oldVisualId = visualEntities.put(piece.id(), playerId);
        despawnVisual(oldVisualId);
    }

    private void onPieceMove(final PieceMoveEvent event) {
        World world = findWorld();
        if (world == null) {
            return;
        }

        Entity piece = event.piece();
        UUID visualId = visualEntities.get(piece.id());
        org.bukkit.entity.Entity bukkitEntity = Bukkit.getEntity(visualId);
        if (bukkitEntity == null) {
            log.warn("기물의 뷰 엔티티를 월드에서 찾을 수 없습니다: pieceId={}, visualId={}", piece.id(), visualId);
            return;
        }

        var gridComponent = piece.component(GridComponent.class);
        Location loc = board.toWorldPos(gridComponent.gridPos()).toLocation(world);

        if (bukkitEntity instanceof Player) {
            loc.setYaw(bukkitEntity.getYaw());
            loc.setPitch(bukkitEntity.getPitch());
        } else {
            loc.setYaw(board.yaw());
        }

        bukkitEntity.teleport(loc);
    }

    private void onPieceClear(final PieceClearEvent event) {
        cleanup();
    }

    private void despawnVisual(final UUID visualId) {
        ActiveMob activeMob = mobManager.getActiveMob(visualId).orElse(null);
        if (activeMob != null) {
            activeMob.despawn();
            return;
        }

        org.bukkit.entity.Entity bukkitEntity = Bukkit.getEntity(visualId);
        if (bukkitEntity != null && !(bukkitEntity instanceof Player)) {
            bukkitEntity.remove();
        }
    }

    private Location toTileLocation(final GridComponent gridComponent, final World world) {
        Location loc = board.toWorldPos(gridComponent.gridPos()).toLocation(world);
        loc.setYaw(board.yaw());
        return loc;
    }

    private World findWorld() {
        World world = Bukkit.getWorld(board.worldId());
        if (world == null) {
            log.error("체스판 월드를 찾을 수 없습니다: worldId={}", board.worldId());
        }

        return world;
    }
}
