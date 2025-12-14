package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.application.port.PieceInfoRenderer;
import dev.tecte.chessWar.game.application.port.PieceSpawner;
import dev.tecte.chessWar.game.domain.exception.PieceSpawnException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.Piece;
import dev.tecte.chessWar.game.domain.model.PieceLayout;
import dev.tecte.chessWar.game.domain.model.PieceSpec;
import dev.tecte.chessWar.game.domain.model.PieceType;
import dev.tecte.chessWar.game.domain.policy.TeamDirectionPolicy;
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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 기물 도메인의 전체 생명주기를 관리하고,
 * 게임 진행에 필요한 기물 관련 비즈니스 로직을 처리하는 서비스입니다.
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
    private final GameTaskScheduler gameTaskScheduler;
    private final TeamService teamService;
    private final PieceSpawner pieceSpawner;
    private final PieceInfoRenderer pieceInfoRenderer;
    private final GameRepository gameRepository;
    private final JavaPlugin plugin;

    /**
     * 기물을 비동기(시차)적으로 스폰하고, 작업의 결과를 담은 Future를 반환합니다.
     * 서버 틱마다 일정량씩 나누어 스폰하여 메인 스레드 부하를 줄입니다.
     *
     * @param world 스폰할 월드
     * @param board 보드 정보
     * @return 성공 시 스폰된 기물들의 맵을 담은 Future, 실패 시 예외를 담은 Future
     */
    @NonNull
    public CompletableFuture<Map<Coordinate, Piece>> spawnPieces(@NonNull World world, @NonNull Board board) {
        var pieceIterator = pieceLayout.pieces().entrySet().iterator();
        CompletableFuture<Map<Coordinate, Piece>> future = new CompletableFuture<>();
        Map<Coordinate, Piece> spawnedPieces = new HashMap<>();

        gameTaskScheduler.scheduleRepeat(task -> {
            try {
                for (int i = 0; i < SPAWN_AMOUNT_PER_TICK; i++) {
                    if (!pieceIterator.hasNext()) {
                        task.cancel();
                        future.complete(spawnedPieces);

                        return;
                    }

                    var entry = pieceIterator.next();
                    Coordinate coordinate = entry.getKey();
                    PieceSpec spec = entry.getValue();
                    Entity spawnedEntity = spawnPieceEntity(spec, coordinate, board, world);
                    Piece piece = Piece.of(spawnedEntity.getUniqueId(), spec);

                    spawnedPieces.put(coordinate, piece);
                }
            } catch (Exception e) {
                task.cancel();
                future.completeExceptionally(e);
            }
        }, SPAWN_TASK_INITIAL_DELAY_TICKS, SPAWN_TASK_PERIOD_TICKS);

        return future;
    }

    /**
     * 게임에 존재하는 모든 기물을 제거합니다.
     *
     * @param game 현재 게임 상태
     */
    public void despawnPieces(@NonNull Game game) {
        for (Piece piece : game.pieces().values()) {
            Entity entity = Bukkit.getEntity(piece.entityId());

            if (entity != null) {
                entity.remove();
            }
        }
    }

    /**
     * 대상 엔티티를 검사하고, 조건이 충족되면 상세 정보를 표시합니다.
     * <p>
     * <b>직업 선택 단계</b>이며 자신의 팀 기물인 경우, 해당 기물의 상세 정보를 보여줍니다. (단, 폰은 제외됩니다.)
     *
     * @param player 정보를 표시할 대상 플레이어
     * @param entity 검사 대상 엔티티
     */
    @HandleException
    public void inspectPiece(@NonNull Player player, @NonNull Entity entity) {
        gameRepository.find()
                .filter(game -> game.phase() == GamePhase.CLASS_SELECTION)
                .flatMap(game -> game.findPiece(entity.getUniqueId()))
                .filter(piece -> piece.spec().type() != PieceType.PAWN)
                .filter(piece -> isFriendlyPiece(player, piece))
                .ifPresent(piece -> pieceInfoRenderer.renderInfo(player, piece));
    }

    /**
     * 게임 내 모든 기물을 각 기물의 적대 팀 플레이어들에게 보이게 합니다.
     *
     * @param game 현재 게임 상태
     */
    public void revealPieces(@NonNull Game game) {
        applyPieceVisibility(game, true);
    }

    /**
     * 게임 내 모든 기물을 각 기물의 적대 팀 플레이어들에게 보이지 않게 숨깁니다.
     *
     * @param game 현재 게임 상태
     */
    public void concealPieces(@NonNull Game game) {
        applyPieceVisibility(game, false);
    }

    /**
     * 특정 플레이어에게 적대 팀의 기물들을 숨깁니다.
     *
     * @param game       현재 게임 상태
     * @param player     대상 플레이어
     * @param playerTeam 대상 플레이어의 팀 (이 팀의 적대 팀 기물을 숨김)
     */
    public void concealEnemyPiecesFor(
            @NonNull Game game,
            @NonNull Player player,
            @NonNull TeamColor playerTeam
    ) {
        List<Entity> enemyEntities = findPieceEntities(game, playerTeam.opposite());

        setEntitiesVisibility(player, enemyEntities, false);
    }

    @NonNull
    private Entity spawnPieceEntity(
            @NonNull PieceSpec spec,
            @NonNull Coordinate coordinate,
            @NonNull Board board,
            @NonNull World world
    ) throws PieceSpawnException {
        Location spawnLocation = board.spawnPositionVector(coordinate).toLocation(world);
        Vector direction = teamDirectionPolicy.calculateFacingVector(spec, board);

        spawnLocation.setDirection(direction);

        return pieceSpawner.spawnPiece(spec, spawnLocation);
    }

    private boolean isFriendlyPiece(@NonNull Player player, @NonNull Piece piece) {
        return teamService.findTeam(player)
                .map(team -> team == piece.spec().teamColor())
                .orElse(false);
    }

    private void applyPieceVisibility(@NonNull Game game, boolean visible) {
        for (TeamColor team : TeamColor.values()) {
            Set<Player> players = teamService.getOnlinePlayers(team);
            List<Entity> enemyEntities = findPieceEntities(game, team.opposite());

            players.forEach(player -> setEntitiesVisibility(player, enemyEntities, visible));
        }
    }

    @NonNull
    private List<Entity> findPieceEntities(@NonNull Game game, @NonNull TeamColor teamColor) {
        return game.pieces().values().stream()
                .filter(piece -> piece.spec().teamColor() == teamColor)
                .map(Piece::entityId)
                .map(Bukkit::getEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void setEntitiesVisibility(
            @NonNull Player player,
            @NonNull List<Entity> entities,
            boolean visible
    ) {
        for (Entity entity : entities) {
            if (visible) {
                player.showEntity(plugin, entity);
            } else {
                player.hideEntity(plugin, entity);
            }
        }
    }
}
