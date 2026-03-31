package dev.tecte.chessWar.piece.infrastructure.mythicmobs;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.policy.TeamFacingPolicy;
import dev.tecte.chessWar.piece.application.port.PieceIdResolver;
import dev.tecte.chessWar.piece.application.port.PieceSpawner;
import dev.tecte.chessWar.piece.domain.exception.PieceSystemException;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.exception.MythicMobSpawnException;
import dev.tecte.chessWar.port.TaskRunner;
import dev.tecte.chessWar.port.WorldResolver;
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
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * MythicMobs 기반으로 기물 소환을 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MythicMobsPieceSpawner implements PieceSpawner {
    private static final int MOB_LEVEL = 1;
    private static final int PIECES_PER_TICK = 4;

    private final TeamFacingPolicy teamFacingPolicy;
    private final PieceIdResolver pieceIdResolver;
    private final WorldResolver worldResolver;
    private final TaskRunner taskRunner;
    private final MobManager mobManager;

    @NonNull
    @Override
    public CompletableFuture<Map<Coordinate, UnitPiece>> spawnAll(
            @NonNull Board board,
            @NonNull Map<Coordinate, PieceSpec> pieceLayout,
            @NonNull CommandSender sender
    ) {
        long initialDelay = 0L;
        long periodTicks = 1L;

        CompletableFuture<Map<Coordinate, UnitPiece>> future = new CompletableFuture<>();
        String worldName = board.worldName();
        World world = worldResolver.resolve(worldName, GameException::worldNotFound);
        var pieceIterator = pieceLayout.entrySet().iterator();
        Map<Coordinate, UnitPiece> spawnedPieces = new HashMap<>();

        taskRunner.runRepeating(
                task -> {
                    if (spawnBatch(task, pieceIterator, spawnedPieces, board, world)) {
                        future.complete(spawnedPieces);
                    }
                },
                initialDelay,
                periodTicks,
                sender,
                "Piece Spawning"
        );

        return future;
    }

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

    private boolean spawnBatch(
            @NonNull BukkitRunnable task,
            @NonNull Iterator<Map.Entry<Coordinate, PieceSpec>> iterator,
            @NonNull Map<Coordinate, UnitPiece> spawnedPieces,
            @NonNull Board board,
            @NonNull World world
    ) {
        for (int i = 0; i < PIECES_PER_TICK; i++) {
            if (!iterator.hasNext()) {
                task.cancel();

                return true;
            }

            var entry = iterator.next();
            Coordinate coordinate = entry.getKey();

            spawnedPieces.put(coordinate, spawnAtCoordinate(coordinate, entry.getValue(), board, world));
        }

        return false;
    }

    private UnitPiece spawnAtCoordinate(Coordinate coordinate, PieceSpec spec, Board board, World world) {
        try {
            Location spawnLocation = board.spawnPositionOf(coordinate).toLocation(world);
            Vector direction = teamFacingPolicy.forwardFacingOf(spec.teamColor(), board);

            spawnLocation.setDirection(direction);

            Entity entity = spawn(spec, spawnLocation);

            return UnitPiece.of(entity.getUniqueId(), spec);
        } catch (Exception e) {
            throw PieceSystemException.spawnFailed(spec, e);
        }
    }
}
