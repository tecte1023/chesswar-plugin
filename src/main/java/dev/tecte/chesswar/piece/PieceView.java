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
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PieceView implements View {
    private final BoardContext board;
    private final MobExecutor mobManager = (MobExecutor) MythicProvider.get().getMobManager();
    private final Map<Integer, UUID> visualEntities = new HashMap<>();

    public static PieceView create(final BoardContext board, final SimulationEventBus eventBus) {
        var view = new PieceView(board);

        eventBus.subscribe(PieceSpawnEvent.class, view::onPieceSpawn);
        eventBus.subscribe(PiecesSpawnEvent.class, view::onPiecesSpawn);
        eventBus.subscribe(PieceBindEvent.class, view::onPieceBind);
        eventBus.subscribe(PieceMoveEvent.class, view::onPieceMove);
        eventBus.subscribe(PieceDeathEvent.class, view::onPieceDeath);
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

        spawnVisual(event.piece(), world);
    }

    private void onPiecesSpawn(final PiecesSpawnEvent event) {
        World world = findWorld();
        if (world == null) {
            return;
        }

        List<Entity> pieces = event.pieces();
        for (Entity piece : pieces) {
            spawnVisual(piece, world);
        }
    }

    private void spawnVisual(final Entity piece, final World world) {
        var pieceComponent = piece.component(PieceComponent.class);
        var gridComponent = piece.component(GridComponent.class);
        String mobId = resolveMobId(pieceComponent.team(), pieceComponent.type());

        MythicMob mythicMob = mobManager.getMythicMob(mobId).orElse(null);
        if (mythicMob == null) {
            log.error("기물의 MythicMob 설정을 찾을 수 없습니다: mobId={}, gridPos={}", mobId, gridComponent.gridPos());
            return;
        }

        Location loc = toTileLocation(gridComponent, pieceComponent.team(), world);
        ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(loc), 1);
        if (activeMob == null) {
            log.error("기물의 MythicMob 소환에 실패했습니다: mobId={}, gridPos={}", mobId, gridComponent.gridPos());
            return;
        }

        org.bukkit.entity.Entity bukkitEntity = activeMob.getEntity().getBukkitEntity();
        if (bukkitEntity instanceof LivingEntity livingEntity) {
            applyPieceAttributes(livingEntity, piece);
        }

        visualEntities.put(piece.id(), bukkitEntity.getUniqueId());
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

        var pieceComponent = piece.component(PieceComponent.class);
        var gridComponent = piece.component(GridComponent.class);
        Location loc = toTileLocation(gridComponent, pieceComponent.team(), world);

        player.teleport(loc);
        applyPieceAttributes(player, piece);
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
            log.warn("기물 엔티티를 월드에서 찾을 수 없습니다: pieceId={}, visualId={}", piece.id(), visualId);
            return;
        }

        var pieceComponent = piece.component(PieceComponent.class);
        var gridComponent = piece.component(GridComponent.class);
        Location loc = board.toWorldPos(gridComponent.gridPos()).toLocation(world);

        if (bukkitEntity instanceof Player) {
            loc.setYaw(bukkitEntity.getYaw());
            loc.setPitch(bukkitEntity.getPitch());
        } else {
            loc.setYaw(resolveTeamYaw(pieceComponent.team()));
        }

        bukkitEntity.teleport(loc);
    }

    private void onPieceDeath(final PieceDeathEvent event) {
        Entity piece = event.piece();
        UUID visualId = visualEntities.remove(piece.id());
        if (visualId == null) {
            log.warn("사망한 기물의 엔티티 정보를 찾을 수 없습니다: pieceId={}", piece.id());
            return;
        }

        org.bukkit.entity.Entity bukkitEntity = Bukkit.getEntity(visualId);
        if (bukkitEntity instanceof Player player) {
            resetPlayerAttributes(player);
        }
    }

    private void onPieceClear(final PieceClearEvent event) {
        cleanup();
    }

    private void applyPieceAttributes(final LivingEntity livingEntity, final Entity piece) {
        var combatComponent = piece.component(CombatComponent.class);
        AttributeInstance maxHealthAttr = livingEntity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr == null) {
            return;
        }

        maxHealthAttr.setBaseValue(combatComponent.maxHp());
        livingEntity.setHealth(combatComponent.hp());
    }

    private void despawnVisual(final UUID visualId) {
        ActiveMob activeMob = mobManager.getActiveMob(visualId).orElse(null);
        if (activeMob != null) {
            activeMob.despawn();
            return;
        }

        org.bukkit.entity.Entity bukkitEntity = Bukkit.getEntity(visualId);
        if (bukkitEntity instanceof Player player) {
            resetPlayerAttributes(player);
            return;
        }

        if (bukkitEntity != null) {
            bukkitEntity.remove();
        }
    }

    private void resetPlayerAttributes(final Player player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr == null) {
            return;
        }

        double defaultMaxHp = maxHealthAttr.getDefaultValue();
        maxHealthAttr.setBaseValue(defaultMaxHp);
        player.setHealth(defaultMaxHp);
    }

    private Location toTileLocation(final GridComponent gridComponent, final Team team, final World world) {
        Location loc = board.toWorldPos(gridComponent.gridPos()).toLocation(world);
        loc.setYaw(resolveTeamYaw(team));
        return loc;
    }

    private float resolveTeamYaw(final Team team) {
        return team == Team.WHITE ? board.yaw() : board.oppositeYaw();
    }

    private String resolveMobId(final Team team, final PieceType type) {
        String teamPrefix = team == Team.WHITE ? "White" : "Black";
        return switch (type) {
            case PAWN -> teamPrefix + "Pawn";
            case KNIGHT -> teamPrefix + "Knight";
            case BISHOP -> teamPrefix + "Bishop";
            case ROOK -> teamPrefix + "Rook";
            case QUEEN -> teamPrefix + "Queen";
            case KING -> teamPrefix + "King";
        };
    }

    private World findWorld() {
        World world = Bukkit.getWorld(board.worldId());
        if (world == null) {
            log.error("체스판이 설치된 월드를 찾을 수 없습니다: worldId={}", board.worldId());
        }

        return world;
    }
}
