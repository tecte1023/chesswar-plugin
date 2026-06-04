package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.Participant;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class PieceManager {
    private static final double PLAYER_TELEPORT_OFFSET_Y = 1.0;
    private static final int INITIAL_MOB_LEVEL = 1;

    private final PieceState pieceState;
    private final MobManager mobManager;
    private final PiecePdcMapper pdcMapper;

    public void spawnInitialLayout(
            final ChessBoard board,
            final Collection<Participant> participants
    ) {
        final Vector forward = board.forwardVector();
        final Vector backward = new Vector(-forward.getX(), -forward.getY(), -forward.getZ());
        final Map<Coordinate, Participant> participantMap = indexParticipants(participants);
        final Location reusableLoc = board.origin().clone();

        for (final Map.Entry<Coordinate, PieceType> entry : ChessFormation.getFullInitialLayout().entrySet()) {
            final Coordinate coordinate = entry.getKey();
            final PieceType type = entry.getValue();
            final Team team = ChessFormation.getTeamAt(coordinate);
            final Vector direction = (team == Team.WHITE) ? forward : backward;
            final Participant participant = participantMap.get(coordinate);
            final Piece piece = (participant != null)
                    ? Piece.of(participant.playerId(), team, type)
                    : Piece.of(null, team, type);

            placePiece(coordinate, piece);
            board.updateToCenterLocation(coordinate, reusableLoc);
            spawnPiece(type, team, coordinate, reusableLoc, direction, false);
        }
    }

    public void spawnPiece(
            final PieceType type,
            final Team team,
            final Coordinate coordinate,
            final Location location,
            final Vector direction,
            final boolean isDisplay
    ) {
        final String mobId = convertToPascalCase(team.name()) + convertToPascalCase(type.name());
        final Optional<MythicMob> mythicMob = mobManager.getMythicMob(mobId);

        if (mythicMob.isEmpty()) {
            return;
        }

        location.setDirection(direction);

        final ActiveMob activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), INITIAL_MOB_LEVEL);

        if (activeMob == null) {
            return;
        }

        final Entity entity = activeMob.getEntity().getBukkitEntity();

        pdcMapper.writeData(entity, type, team, coordinate, isDisplay);
        addSpawnedEntity(entity);

        if (isDisplay) {
            return;
        }

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        registerPieceEntity(coordinate, living);
    }

    public void movePiece(final ChessBoard board, final Coordinate from, final Coordinate to) {
        final Map<Coordinate, Piece> boardPieces = pieceState.boardPieces();
        final Piece piece = boardPieces.get(from);

        if (piece == null) {
            return;
        }

        relocateEntity(board, from, to);
        boardPieces.remove(from);
        boardPieces.put(to, piece);
    }

    public void capturePiece(
            final ChessBoard board,
            final Coordinate attacker,
            final Coordinate victim
    ) {
        final Map<Coordinate, Piece> boardPieces = pieceState.boardPieces();
        final Piece victimPiece = boardPieces.get(victim);

        if (victimPiece == null) {
            return;
        }

        removePiece(victim);
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

        final Optional<Coordinate> coordinate = pdcMapper.readCoordinate(entity);

        if (coordinate.isPresent()) {
            removePiece(coordinate.get());
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
        pieceState.boardPieces().put(coordinate, piece);

        if (piece.ownerId() == null) {
            return;
        }

        pieceState.entityToCoordinate().put(piece.ownerId(), coordinate);
    }

    public void registerPieceEntity(final Coordinate coordinate, final LivingEntity entity) {
        pieceState.pieceEntities().put(coordinate, entity);
        pieceState.entityToCoordinate().put(entity.getUniqueId(), coordinate);
    }

    public void removePiece(final Coordinate coordinate) {
        removeBoardPieceMapping(coordinate);
        removePieceEntityMapping(coordinate);
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

        final UUID entityId = entity.getUniqueId();

        pieceState.entityToCoordinate().remove(entityId);
        pieceState.spawnedEntities().remove(entityId);
    }

    private void removeBoardPieceMapping(final Coordinate coordinate) {
        final Piece piece = pieceState.boardPieces().remove(coordinate);

        if (piece == null || piece.ownerId() == null) {
            return;
        }

        pieceState.entityToCoordinate().remove(piece.ownerId());
    }

    private void removePieceEntityMapping(final Coordinate coordinate) {
        final LivingEntity entity = pieceState.pieceEntities().remove(coordinate);

        if (entity == null) {
            return;
        }

        pieceState.entityToCoordinate().remove(entity.getUniqueId());
    }

    private void relocateEntity(final ChessBoard board, final Coordinate from, final Coordinate to) {
        if (board == null) {
            return;
        }

        final Location targetLocation = board.updateToCenterLocation(to, board.origin().clone());

        relocateMobEntity(from, to, targetLocation);
        targetLocation.add(0, PLAYER_TELEPORT_OFFSET_Y, 0);
        relocatePlayerEntity(from, to, targetLocation);
    }

    private void relocateMobEntity(
            final Coordinate from,
            final Coordinate to,
            final Location targetLocation
    ) {
        final Map<Coordinate, LivingEntity> pieceEntities = pieceState.pieceEntities();
        final LivingEntity entity = pieceEntities.get(from);

        if (entity == null) {
            return;
        }

        entity.teleport(targetLocation);
        pdcMapper.updateCoordinate(entity, to);
        pieceEntities.remove(from);
        pieceEntities.put(to, entity);
        pieceState.entityToCoordinate().put(entity.getUniqueId(), to);
    }

    private void relocatePlayerEntity(
            final Coordinate from,
            final Coordinate to,
            final Location targetLocation
    ) {
        final Piece piece = pieceState.boardPieces().get(from);

        if (piece == null || !piece.isPlayerPiece()) {
            return;
        }

        final Player player = Bukkit.getPlayer(piece.ownerId());

        if (player == null) {
            return;
        }

        player.teleport(targetLocation);
        pieceState.entityToCoordinate().put(player.getUniqueId(), to);
    }

    private Map<Coordinate, Participant> indexParticipants(
            final Collection<Participant> participants
    ) {
        final Map<Coordinate, Participant> map = new HashMap<>();

        for (final Participant participant : participants) {
            if (participant.initialCoordinate() == null) {
                continue;
            }

            map.put(participant.initialCoordinate(), participant);
        }

        return map;
    }

    private static String convertToPascalCase(final String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }

        return source.substring(0, 1).toUpperCase() + source.substring(1).toLowerCase();
    }
}
