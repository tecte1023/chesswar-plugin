package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.team.Team;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class PieceManager {
    private final Map<Coordinate, Piece> boardPieces = new HashMap<>();
    private final Map<Coordinate, UUID> pieceEntities = new HashMap<>();
    private final Set<UUID> spawnedEntities = new HashSet<>();

    public void placePiece(Coordinate coordinate, Piece piece) {
        boardPieces.put(coordinate, piece);
    }

    public void registerPieceEntity(Coordinate coordinate, UUID entityId) {
        pieceEntities.put(coordinate, entityId);
    }

    public void removePiece(Coordinate coordinate) {
        boardPieces.remove(coordinate);
        pieceEntities.remove(coordinate);
    }

    public Optional<Piece> findPieceAt(Coordinate coordinate) {
        return Optional.ofNullable(boardPieces.get(coordinate));
    }

    public void addSpawnedEntity(UUID entityId) {
        spawnedEntities.add(entityId);
    }

    public void clearSpawnedEntities(Plugin plugin, boolean onlyBarracks) {
        NamespacedKey isBarracksKey = new NamespacedKey(plugin, "is_barracks_entity");

        for (java.util.Iterator<UUID> it = spawnedEntities.iterator(); it.hasNext(); ) {
            UUID entityId = it.next();
            Entity entity = Bukkit.getEntity(entityId);

            if (entity != null) {
                if (onlyBarracks) {
                    Byte isBarracks = entity.getPersistentDataContainer().get(isBarracksKey, PersistentDataType.BYTE);
                    if (isBarracks != null && isBarracks == 1) {
                        entity.remove();
                        it.remove();
                    }
                } else {
                    entity.remove();
                    it.remove();
                }
            } else {
                it.remove();
            }
        }
    }

    public void spawnPiece(
            Plugin plugin,
            Location location,
            PieceType type,
            Team team,
            Coordinate logicCoord,
            Vector direction,
            boolean isBarracks
    ) {
        MobManager mobManager = MythicProvider.get().getMobManager();
        String mobId = toPascalCase(team.name()) + toPascalCase(type.name());

        mobManager.getMythicMob(mobId).ifPresent(mythicMob -> {
            location.setDirection(direction);
            ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), 1);

            if (activeMob == null) {
                return;
            }

            Entity entity = activeMob.getEntity().getBukkitEntity();
            NamespacedKey typeKey = new NamespacedKey(plugin, "barracks_piece_type");
            NamespacedKey teamKey = new NamespacedKey(plugin, "barracks_piece_team");
            NamespacedKey coordXKey = new NamespacedKey(plugin, "barracks_piece_x");
            NamespacedKey coordYKey = new NamespacedKey(plugin, "barracks_piece_y");
            NamespacedKey isBarracksKey = new NamespacedKey(plugin, "is_barracks_entity");

            entity.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
            entity.getPersistentDataContainer().set(teamKey, PersistentDataType.STRING, team.name());
            entity.getPersistentDataContainer().set(coordXKey, PersistentDataType.INTEGER, logicCoord.x());
            entity.getPersistentDataContainer().set(coordYKey, PersistentDataType.INTEGER, logicCoord.y());
            entity.getPersistentDataContainer().set(isBarracksKey, PersistentDataType.BYTE, (byte) (isBarracks ? 1 : 0));
            
            addSpawnedEntity(entity.getUniqueId());

            if (!isBarracks) {
                registerPieceEntity(logicCoord, entity.getUniqueId());
            }
        });
    }

    private String toPascalCase(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        return source.substring(0, 1).toUpperCase() + source.substring(1).toLowerCase();
    }

    public void applyStats(Player player, PieceType type) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(type.baseHealth());
            player.setHealth(type.baseHealth());
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(type.baseDamage());
        }
    }

    public void resetStats(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0);
            player.setHealth(20.0);
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(1.0);
        }
    }

    public void reset() {
        boardPieces.clear();
        pieceEntities.clear();
        spawnedEntities.clear();
    }
}
