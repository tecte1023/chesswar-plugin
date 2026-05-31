package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.team.Team;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.tecte.chesswar.game.component.GameResetEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@Accessors(fluent = true)
public class PieceManager implements Listener {
    private final ChessWar plugin;
    private final Map<Coordinate, Piece> boardPieces = new HashMap<>();
    private final Map<Coordinate, LivingEntity> pieceEntities = new HashMap<>();
    private final Set<UUID> spawnedEntities = new HashSet<>();

    private final NamespacedKey typeKey;
    private final NamespacedKey teamKey;
    private final NamespacedKey coordXKey;
    private final NamespacedKey coordYKey;
    private final NamespacedKey isBarracksKey;

    public PieceManager(ChessWar plugin) {
        this.plugin = plugin;
        typeKey = new NamespacedKey(plugin, "barracks_piece_type");
        teamKey = new NamespacedKey(plugin, "barracks_piece_team");
        coordXKey = new NamespacedKey(plugin, "barracks_piece_x");
        coordYKey = new NamespacedKey(plugin, "barracks_piece_y");
        isBarracksKey = new NamespacedKey(plugin, "is_barracks_entity");
    }

    @EventHandler
    public void onGameReset(GameResetEvent event) {
        clearSpawnedEntities(false);
        reset();
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetStats(player);
            PieceItemUtils.removePlayerPieceItems(player);
        }
    }

    public void spawnInitialLayout(final ChessBoard mainBoard, final Collection<Participant> participants) {
        ChessFormation.getFullInitialLayout().forEach((coord, type) -> {
            final Team team = ChessFormation.getTeamAt(coord);
            final Vector direction = (team == Team.WHITE) ? mainBoard.forward().getDirection() : mainBoard.forward().getDirection().multiply(-1);

            final Optional<Participant> participant = participants.stream()
                    .filter(p -> coord.equals(p.initialCoordinate()))
                    .findFirst();

            final Piece piece = participant.map(p -> Piece.of(p.playerId(), team, type))
                    .orElseGet(() -> Piece.of(null, team, type));

            placePiece(coord, piece);
            spawnPiece(
                    mainBoard.toCenterLocation(coord),
                    type,
                    team,
                    coord,
                    direction,
                    false
            );
        });
    }

    public void setupMythicMobs(Plugin plugin) {
        Plugin mythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs");

        if (mythicMobs == null) {
            return;
        }

        File targetFile = new File(new File(mythicMobs.getDataFolder(), "Mobs"), "Piece.yml");

        if (targetFile.exists()) {
            return;
        }

        File parentDir = targetFile.getParentFile();

        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            log.error("Failed to create MythicMobs piece directory.");
            return;
        }

        try (InputStream in = plugin.getResource("mobs/Piece.yml")) {
            if (in != null) {
                Files.copy(in, targetFile.toPath());
                log.info("Successfully synced Piece.yml to MythicMobs folder.");
            }
        } catch (IOException e) {
            log.error("Failed to sync Piece.yml: {}", e.getMessage());
        }
    }

    public void placePiece(Coordinate coordinate, Piece piece) {
        boardPieces.put(coordinate, piece);
    }

    public void registerPieceEntity(Coordinate coordinate, LivingEntity entity) {
        pieceEntities.put(coordinate, entity);
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

    public void purgeEntity(Entity entity) {
        if (entity == null) return;
        if (entity instanceof LivingEntity living) {
            pieceEntities.values().removeIf(e -> e.equals(living));
        }
        spawnedEntities.remove(entity.getUniqueId());
    }

    public void clearSpawnedEntities(boolean onlyBarracks) {
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

            entity.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
            entity.getPersistentDataContainer().set(teamKey, PersistentDataType.STRING, team.name());
            entity.getPersistentDataContainer().set(coordXKey, PersistentDataType.INTEGER, logicCoord.x());
            entity.getPersistentDataContainer().set(coordYKey, PersistentDataType.INTEGER, logicCoord.y());
            entity.getPersistentDataContainer().set(isBarracksKey, PersistentDataType.BYTE, (byte) (isBarracks ? 1 : 0));
            
            addSpawnedEntity(entity.getUniqueId());

            if (!isBarracks && entity instanceof LivingEntity living) {
                registerPieceEntity(logicCoord, living);
            }
        });
    }

    public void handlePieceDeath(Entity entity) {
        if (!spawnedEntities.contains(entity.getUniqueId())) {
            return;
        }

        Integer x = entity.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
        Integer y = entity.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);

        if (x != null && y != null) {
            removePiece(Coordinate.of(x, y));
        }
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

    public Optional<Coordinate> findCoordinateByEntity(final Entity entity) {
        if (entity instanceof Player player) {
            return boardPieces.entrySet().stream()
                    .filter(entry -> player.getUniqueId().equals(entry.getValue().ownerId()))
                    .map(Map.Entry::getKey)
                    .findFirst();
        }

        return pieceEntities.entrySet().stream()
                .filter(entry -> entry.getValue().equals(entity))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public void movePiece(final Coordinate from, final Coordinate to) {
        final Piece piece = boardPieces.get(from);
        if (piece == null) return;

        relocateEntity(from, to);
        boardPieces.remove(from);
        boardPieces.put(to, piece);
    }

    public void capturePiece(final Coordinate attackerCoord, final Coordinate victimCoord) {
        final Piece victim = boardPieces.get(victimCoord);
        if (victim == null) return;

        removePiece(victimCoord);
        movePiece(attackerCoord, victimCoord);
    }

    private void relocateEntity(final Coordinate from, final Coordinate to) {
        final LivingEntity entity = pieceEntities.get(from);
        final ChessBoard board = plugin.boardManager().currentBoard();
        if (board == null) return;

        final Location targetLocation = board.toCenterLocation(to);

        if (entity != null) {
            entity.teleport(targetLocation);
            entity.getPersistentDataContainer().set(coordXKey, PersistentDataType.INTEGER, to.x());
            entity.getPersistentDataContainer().set(coordYKey, PersistentDataType.INTEGER, to.y());

            pieceEntities.remove(from);
            pieceEntities.put(to, entity);
        }

        final Piece piece = boardPieces.get(from);
        if (piece != null && piece.isPlayerPiece()) {
            final Player player = Bukkit.getPlayer(piece.ownerId());
            if (player != null) {
                player.teleport(targetLocation.clone().add(0, 1, 0));
            }
        }
    }
}
