package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.BoardComponent;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.CombatPolicy;
import dev.tecte.chesswar.game.event.PieceSpawnEvent;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.team.Team;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j(topic = "ChessWar")
public class PieceManager {
    private static final double PLAYER_TELEPORT_OFFSET_Y = 1.0;
    private static final int INITIAL_MOB_LEVEL = 1;

    private static final double BUNKER_Y = -1024.0;
    private static final double BUNKER_GRID_SIZE = 2.0;

    private final org.bukkit.plugin.Plugin plugin;
    private final PieceState pieceState;
    private final ActionPatternTable actionPatternTable;
    private final MobManager mobManager;
    private final PiecePdcMapper pdcMapper;
    private final BoardComponent boardComponent;
    private final BoardManager boardManager;
    private final MoveValidator moveValidator;
    private final CombatPolicy combatPolicy;
    private final GameAnnouncer announcer;
    private final PieceVisualManager visualManager;

    public void spawnBunker(final ChessBoard board) {
        clearSpawnedEntities(false);
        pieceState.reset();
        final Location bunkerLoc = board.origin();
        bunkerLoc.setY(BUNKER_Y);

        for (final Map.Entry<Coordinate, PieceType> entry : PieceLayout.getFullInitialLayout().entrySet()) {
            final Coordinate coord = entry.getKey();
            final PieceType type = entry.getValue();
            final Team team = PieceLayout.getTeamAt(coord);

            final Location loc = bunkerLoc.clone().add(coord.x() * BUNKER_GRID_SIZE, 0, coord.y() * BUNKER_GRID_SIZE);
            loc.setDirection(board.calculateDirection(team));

            final LivingEntity entity = spawnBunkerPiece(type, team, coord, loc);
            if (entity != null) {
                final Piece piece = Piece.of(entity.getUniqueId(), team, type);
                placePiece(coord, piece);
            }
        }
    }

    private LivingEntity spawnBunkerPiece(
            final PieceType type,
            final Team team,
            final Coordinate coordinate,
            final Location location
    ) {
        final String mobId = convertToPascalCase(team.name()) + convertToPascalCase(type.name());
        final MythicMob mythicMob = mobManager.getMythicMob(mobId).orElse(null);

        if (mythicMob == null) {
            return null;
        }

        final ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), INITIAL_MOB_LEVEL);
        if (activeMob == null) {
            return null;
        }

        final Entity entity = activeMob.getEntity().getBukkitEntity();
        pdcMapper.writeData(entity, type, team, coordinate, false);
        addSpawnedEntity(entity);

        if (!(entity instanceof final LivingEntity living)) {
            return null;
        }

        living.setAI(false);
        living.setGravity(false);

        visualManager.setupModel(living);

        final org.bukkit.attribute.AttributeInstance maxHealthAttr = living.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(type.baseHealth());
        }
        living.setHealth(type.baseHealth());

        Bukkit.getPluginManager().callEvent(new PieceSpawnEvent(living, type, team));
        return living;
    }

    public void deployBunkerToBarracks() {
        final Piece[] occupancy = pieceState.boardOccupancy();

        for (int i = 0; i < Coordinate.SQUARE_COUNT; i++) {
            final Piece piece = occupancy[i];

            if (piece == null) {
                continue;
            }

            final LivingEntity entity = piece.isPlayer() ? Bukkit.getPlayer(piece.id()) : (Bukkit.getEntity(piece.id()) instanceof LivingEntity e ? e : null);

            if (entity == null) {
                continue;
            }

            final Team team = piece.team();
            // TODO: [Refactoring Phase 2] BoardManager 수정에 따른 후속 작업 필요
            final ChessBoard barracksBoard = boardComponent.board();

            if (barracksBoard == null) {
                continue;
            }

            final Coordinate coord = Coordinate.of(i % Coordinate.BOARD_SIZE, i / Coordinate.BOARD_SIZE);
            final Location targetLoc = barracksBoard.toCenterLocation(coord);
            targetLoc.setDirection(barracksBoard.calculateDirection(team));

            entity.teleport(targetLoc);
        }
    }

    public void deployBunkerToBattlefield(final ChessBoard board) {
        final Location targetLoc = board.origin();
        final Piece[] occupancy = pieceState.boardOccupancy();

        for (int i = 0; i < Coordinate.SQUARE_COUNT; i++) {
            final Piece existingPiece = occupancy[i];

            if (existingPiece == null) {
                continue;
            }

            final LivingEntity entity = existingPiece.isPlayer() ? Bukkit.getPlayer(existingPiece.id()) : (Bukkit.getEntity(existingPiece.id()) instanceof LivingEntity e ? e : null);

            if (entity == null) {
                continue;
            }

            if (existingPiece.isPlayer()) {
                entity.remove();
                continue;
            }

            final Coordinate coordinate = Coordinate.of(i % Coordinate.BOARD_SIZE, i / Coordinate.BOARD_SIZE);
            board.updateToCenterLocation(coordinate, targetLoc);
            targetLoc.setDirection(board.calculateDirection(existingPiece.team()));
            entity.teleport(targetLoc);
            visualManager.snapModel(entity);
        }
    }

    @Nullable
    public LivingEntity spawnPiece(
            final PieceType type,
            final Team team,
            final Coordinate coordinate,
            final Location location,
            final Vector direction,
            final boolean isDisplay
    ) {
        final String mobId = convertToPascalCase(team.name()) + convertToPascalCase(type.name());
        final MythicMob mythicMob = mobManager.getMythicMob(mobId).orElse(null);

        if (mythicMob == null) {
            return null;
        }

        location.setDirection(direction);

        final ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), INITIAL_MOB_LEVEL);

        if (activeMob == null) {
            return null;
        }

        final Entity entity = activeMob.getEntity().getBukkitEntity();

        pdcMapper.writeData(entity, type, team, coordinate, isDisplay);
        addSpawnedEntity(entity);

        if (isDisplay) {
            return null;
        }

        if (!(entity instanceof final LivingEntity living)) {
            return null;
        }

        Bukkit.getPluginManager().callEvent(new PieceSpawnEvent(living, type, team));

        return living;
    }

    public void movePiece(final ChessBoard board, final Coordinate from, final Coordinate to) {
        final Piece piece = pieceState.piece(from);

        if (piece == null) {
            return;
        }

        relocateEntity(board, from, to);
        if (!pieceState.movePiece(from, to)) {
            log.error("Failed to move piece from {} to {}! Destination might be occupied.", from, to);
        }
    }

    public void capturePiece(
            final ChessBoard board,
            final Coordinate attacker,
            final Coordinate victim
    ) {
        final Piece victimPiece = pieceState.piece(victim);

        if (victimPiece != null) {
            final LivingEntity victimEntity = victimPiece.isPlayer() ? Bukkit.getPlayer(victimPiece.id()) : (Bukkit.getEntity(victimPiece.id()) instanceof LivingEntity e ? e : null);
            if (victimEntity != null) {
                purgeEntity(victimEntity);
            }

            removePiece(victim);
        }

        movePiece(board, attacker, victim);
    }

    public void processFullGameReset() {
        clearSpawnedEntities(false);
        pieceState.reset();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            PieceItemUtils.removePlayerPieceItems(player);
        }
    }

    public boolean handlePieceDisappearance(final Entity entity) {
        if (entity == null) {
            return false;
        }

        if (!pieceState.spawnedEntities().contains(entity.getUniqueId())) {
            return false;
        }

        if (!entity.isDead()) {
            return false;
        }

        final Coordinate coordinate = pdcMapper.readCoordinate(entity);

        if (coordinate != null) {
            final Piece coordPiece = pieceState.piece(coordinate);
            final LivingEntity registeredEntity = coordPiece != null ? (coordPiece.isPlayer() ? Bukkit.getPlayer(coordPiece.id()) : (Bukkit.getEntity(coordPiece.id()) instanceof LivingEntity e ? e : null)) : null;

            if (registeredEntity != null && registeredEntity.getUniqueId().equals(entity.getUniqueId())) {
                removePiece(coordinate);
            } else if (registeredEntity == null && pieceState.hasPiece(coordinate) && !pieceState.piece(coordinate).isPlayer()) {
                removePiece(coordinate);
            }
        }

        purgeEntity(entity);
        return true;
    }

    public void clearSpawnedEntities(final boolean onlyDisplay) {
        final Set<UUID> spawnedEntities = pieceState.spawnedEntities();
        final List<UUID> targetIds = new java.util.ArrayList<>(spawnedEntities);

        if (!onlyDisplay) {
            for (final UUID entityId : targetIds) {
                final Entity entity = Bukkit.getEntity(entityId);

                if (entity == null) {
                    spawnedEntities.remove(entityId);
                    continue;
                }

                entity.remove();
            }

            spawnedEntities.clear();
            return;
        }

        for (final UUID entityId : targetIds) {
            final Entity entity = Bukkit.getEntity(entityId);

            if (entity == null) {
                spawnedEntities.remove(entityId);
                continue;
            }

            if (!pdcMapper.isDisplay(entity)) {
                continue;
            }

            entity.remove();
            spawnedEntities.remove(entityId);
        }
    }

    public void placePiece(final Coordinate coordinate, final Piece piece) {
        if (!pieceState.addPiece(coordinate, piece)) {
            log.error("Failed to place piece at {}! Coordinate might be occupied.", coordinate);
        }
    }


    public void removePiece(final Coordinate coordinate) {
        final Piece piece = pieceState.piece(coordinate);
        if (piece != null) {
            final LivingEntity entity = piece.isPlayer() ? Bukkit.getPlayer(piece.id()) : (Bukkit.getEntity(piece.id()) instanceof LivingEntity e ? e : null);
            if (entity != null) {
                piece.currentHealth(entity.getHealth());
            }
        }
        pieceState.removePiece(coordinate);
    }

    public void addSpawnedEntity(final Entity entity) {
        if (entity == null) {
            return;
        }

        pieceState.spawnedEntities().add(entity.getUniqueId());
    }

    public void purgeEntity(final Entity entity) {
        if (entity == null) {
            return;
        }

        pieceState.purgeEntity(entity.getUniqueId());
    }

    private void relocateEntity(final ChessBoard board, final Coordinate from, final Coordinate to) {
        if (board == null) {
            return;
        }

        final Piece piece = pieceState.piece(from);
        if (piece == null) {
            return;
        }

        final Location mobTarget = board.updateToCenterLocation(to, board.origin());
        mobTarget.setDirection(board.calculateDirection(piece.team()));

        final LivingEntity mobEntity = piece.isPlayer() ? Bukkit.getPlayer(piece.id()) : (Bukkit.getEntity(piece.id()) instanceof LivingEntity e ? e : null);
        if (mobEntity != null) {
            mobEntity.teleport(mobTarget);
            visualManager.snapModel(mobEntity);
            pdcMapper.updateCoordinate(mobEntity, to);
        }

        if (piece.isPlayer()) {
            final Player player = Bukkit.getPlayer(piece.id());
            if (player != null) {
                final Location playerStart = player.getLocation();
                final Location playerEnd = mobTarget.clone().add(0, PLAYER_TELEPORT_OFFSET_Y, 0);

                if (playerStart.distanceSquared(playerEnd) > 1.0) {
                    player.teleport(playerEnd);
                }
            }
        }
    }

    @Nullable
    public Coordinate findCoordinate(final org.bukkit.entity.Entity entity) {
        if (entity == null) {
            return null;
        }

        final Coordinate memoryCoord = pieceState.coordinate(entity.getUniqueId());
        if (memoryCoord != null) {
            return memoryCoord;
        }

        return pdcMapper.readCoordinate(entity);
    }

    public void registerPlayerPiece(final Player player, final Team team, final PieceType type, final Coordinate coordinate) {
        final Piece piece = Piece.ofPlayer(player.getUniqueId(), team, type);
        attachAbilities(piece);
        pdcMapper.writeData(player, type, team, coordinate, false);
        placePiece(coordinate, piece);
    }

    public void attachAbilities(final Piece piece) {
        final PieceAbility[] abilities = switch (piece.type()) {
            case ROOK -> new PieceAbility[]{new RookAbility(announcer, 1.0)};
            case KNIGHT -> new PieceAbility[]{new KnightAbility(pieceState, announcer)};
            case BISHOP -> new PieceAbility[]{new BishopAbility(pieceState, announcer, 1.0)};
            case PAWN -> new PieceAbility[]{new PawnAbility()};
            case QUEEN -> new PieceAbility[]{
                    new RookAbility(announcer, 0.5),
                    new BishopAbility(pieceState, announcer, 0.5)
            };
            case KING -> new PieceAbility[]{new KingAbility(pieceState, combatPolicy, announcer)};
            default -> new PieceAbility[0];
        };
        piece.ability().abilities(abilities);
    }

    public ActionMaskComponent getValidMask(final Coordinate position, final Piece piece) {
        if (piece.actionMask().lastVersion() != pieceState.occupancyVersion()) {
            final ActionPattern pattern = actionPatternTable.patternFor(piece.type());
            pattern.updateAction(pieceState.boardOccupancy(), piece, position, piece.actionMask());
            piece.actionMask().lastVersion(pieceState.occupancyVersion());
        }
        return piece.actionMask();
    }

    private static String convertToPascalCase(final String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }

        return source.substring(0, 1).toUpperCase() + source.substring(1).toLowerCase();
    }
}
