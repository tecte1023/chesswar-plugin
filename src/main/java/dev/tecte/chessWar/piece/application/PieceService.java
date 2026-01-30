package dev.tecte.chessWar.piece.application;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.application.port.GameTaskScheduler;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.policy.TeamDirectionPolicy;
import dev.tecte.chessWar.piece.application.port.PieceSpawner;
import dev.tecte.chessWar.piece.domain.exception.PieceSystemException;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.PieceLayout;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.team.application.TeamService;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 기물의 생명주기와 비즈니스 로직을 처리하는 서비스입니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceService {
    private static final int SPAWN_AMOUNT_PER_TICK = 4;
    private static final long SPAWN_TASK_INITIAL_DELAY_TICKS = 0L;
    private static final long SPAWN_TASK_PERIOD_TICKS = 1L;

    private final PieceLayout pieceLayout;
    private final TeamDirectionPolicy teamDirectionPolicy;
    private final TeamService teamService;
    private final PieceSpawner pieceSpawner;
    private final GameTaskScheduler gameTaskScheduler;
    private final JavaPlugin plugin;

    /**
     * 시차를 두어 기물을 비동기적으로 소환합니다.
     * <p>
     * 서버 틱마다 일정량씩 나누어 소환하여 메인 스레드 부하를 줄입니다.
     *
     * @param board 체스판
     * @param world 소환할 월드
     * @return 소환된 기물 배치
     */
    @NonNull
    public CompletableFuture<Map<Coordinate, UnitPiece>> spawnPieces(@NonNull Board board, @NonNull World world) {
        var pieceIterator = pieceLayout.pieces().entrySet().iterator();
        CompletableFuture<Map<Coordinate, UnitPiece>> future = new CompletableFuture<>();
        Map<Coordinate, UnitPiece> spawnedPieces = new HashMap<>();

        gameTaskScheduler.scheduleRepeat(
                task -> spawnBatch(task, pieceIterator, spawnedPieces, board, world, future),
                SPAWN_TASK_INITIAL_DELAY_TICKS,
                SPAWN_TASK_PERIOD_TICKS
        );

        return future;
    }

    /**
     * 주어진 기물 목록의 모든 기물을 제거합니다.
     *
     * @param pieces 제거할 기물 목록
     */
    public void despawnPieces(@NonNull Collection<UnitPiece> pieces) {
        pieces.forEach(piece -> pieceSpawner.despawn(piece.id()));
    }

    /**
     * 모든 플레이어가 적대 팀의 기물을 볼 수 있게 합니다.
     *
     * @param game 현재 게임 상태
     */
    public void revealPieces(@NonNull Game game) {
        applyPieceVisibility(game, true);
    }

    /**
     * 모든 플레이어가 적대 팀의 기물을 볼 수 없게 합니다.
     *
     * @param game 현재 게임 상태
     */
    public void concealPieces(@NonNull Game game) {
        applyPieceVisibility(game, false);
    }

    /**
     * 특정 플레이어가 적대 팀의 기물을 볼 수 없게 합니다.
     *
     * @param game       현재 게임 상태
     * @param player     대상 플레이어
     * @param playerTeam 대상 플레이어의 팀
     */
    public void concealEnemyPiecesFor(
            @NonNull Game game,
            @NonNull Player player,
            @NonNull TeamColor playerTeam
    ) {
        setEntitiesVisibility(player, findPieceEntities(game, playerTeam.opposite()), false);
    }

    private void spawnBatch(
            @NonNull BukkitRunnable task,
            @NonNull Iterator<Map.Entry<Coordinate, PieceSpec>> iterator,
            @NonNull Map<Coordinate, UnitPiece> spawnedPieces,
            @NonNull Board board,
            @NonNull World world,
            @NonNull CompletableFuture<Map<Coordinate, UnitPiece>> future
    ) {
        try {
            for (int i = 0; i < SPAWN_AMOUNT_PER_TICK; i++) {
                if (!iterator.hasNext()) {
                    task.cancel();
                    future.complete(spawnedPieces);

                    return;
                }

                var entry = iterator.next();
                Coordinate coordinate = entry.getKey();

                spawnedPieces.put(coordinate, spawnPiece(coordinate, entry.getValue(), board, world));
            }
        } catch (Exception e) {
            task.cancel();
            future.completeExceptionally(e);
        }
    }

    @NonNull
    private UnitPiece spawnPiece(
            @NonNull Coordinate coordinate,
            @NonNull PieceSpec spec,
            @NonNull Board board,
            @NonNull World world
    ) {
        try {
            return UnitPiece.of(spawnPieceEntity(coordinate, spec, board, world), spec);
        } catch (Exception e) {
            throw PieceSystemException.spawnFailed(spec, e);
        }
    }

    @NonNull
    private UUID spawnPieceEntity(
            @NonNull Coordinate coordinate,
            @NonNull PieceSpec spec,
            @NonNull Board board,
            @NonNull World world
    ) {
        Location spawnLocation = board.spawnPositionVector(coordinate).toLocation(world);
        Vector direction = teamDirectionPolicy.calculateFacingVector(spec.teamColor(), board);

        spawnLocation.setDirection(direction);

        return pieceSpawner.spawn(spec, spawnLocation).getUniqueId();
    }

    private void applyPieceVisibility(@NonNull Game game, boolean visible) {
        for (TeamColor team : TeamColor.values()) {
            var players = teamService.getOnlinePlayers(team);
            var enemyEntities = findPieceEntities(game, team.opposite());

            players.forEach(player -> setEntitiesVisibility(player, enemyEntities, visible));
        }
    }

    @NonNull
    private List<Entity> findPieceEntities(@NonNull Game game, @NonNull TeamColor teamColor) {
        return game.findPiecesByTeam(teamColor)
                .map(Piece::id)
                .map(Bukkit::getEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void setEntitiesVisibility(
            @NonNull Player player,
            @NonNull List<Entity> entities,
            boolean visible
    ) {
        entities.forEach(entity -> {
            if (visible) {
                player.showEntity(plugin, entity);
            } else {
                player.hideEntity(plugin, entity);
            }
        });
    }
}
