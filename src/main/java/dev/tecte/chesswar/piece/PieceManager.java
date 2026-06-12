package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.CombatPolicy;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.event.PieceSpawnEvent;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.piece.ability.BishopAbility;
import dev.tecte.chesswar.piece.ability.KingAbility;
import dev.tecte.chesswar.piece.ability.KnightAbility;
import dev.tecte.chesswar.piece.ability.PawnAbility;
import dev.tecte.chesswar.piece.ability.RookAbility;
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

    private static final double BUNKER_Y = -1024.0;
    private static final double BUNKER_GRID_SIZE = 2.0;

    private final org.bukkit.plugin.Plugin plugin;
    private final PieceState pieceState;
    private final MobManager mobManager;
    private final PiecePdcMapper pdcMapper;
    private final BoardManager boardManager;
    private final MoveValidator moveValidator;
    private final CombatPolicy combatPolicy;
    private final GameAnnouncer announcer;
    private final Map<Coordinate, LivingEntity> bunkerEntities = new HashMap<>();

    public void spawnBunker(final ChessBoard board) {
        clearBunker();
        final Location bunkerLoc = board.origin().clone();
        bunkerLoc.setY(BUNKER_Y);

        final Vector forward = board.forwardVector();
        final Vector backward = new Vector(-forward.getX(), -forward.getY(), -forward.getZ());

        for (final Map.Entry<Coordinate, PieceType> entry : ChessFormation.getFullInitialLayout().entrySet()) {
            final Coordinate coord = entry.getKey();
            final PieceType type = entry.getValue();
            final Team team = ChessFormation.getTeamAt(coord);
            final Vector direction = (team == Team.WHITE) ? forward : backward;

            final Location loc = bunkerLoc.clone().add(coord.x() * BUNKER_GRID_SIZE, 0, coord.y() * BUNKER_GRID_SIZE);
            loc.setDirection(direction);

            final LivingEntity entity = spawnBunkerPiece(type, team, coord, loc);
            if (entity != null) {
                bunkerEntities.put(coord, entity);
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
        final Optional<MythicMob> mythicMob = mobManager.getMythicMob(mobId);

        if (mythicMob.isEmpty()) {
            return null;
        }

        final ActiveMob activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), INITIAL_MOB_LEVEL);
        if (activeMob == null) {
            return null;
        }

        final Entity entity = activeMob.getEntity().getBukkitEntity();
        pdcMapper.writeData(entity, type, team, coordinate, false);
        addSpawnedEntity(entity);

        if (!(entity instanceof LivingEntity living)) {
            return null;
        }

        living.setAI(false);
        living.setGravity(false);
        living.setInvulnerable(true);
        // 바닐라 투명화는 모델링에 영향을 줄 수 있으므로 일단 보류 (MythicMobs 옵션에서 처리 권장)

        Bukkit.getPluginManager().callEvent(new PieceSpawnEvent(living, type, team));
        return living;
    }

    public void deployBunkerToBarracks() {
        for (final Map.Entry<Coordinate, LivingEntity> entry : bunkerEntities.entrySet()) {
            final Coordinate coord = entry.getKey();
            final LivingEntity entity = entry.getValue();
            final Team team = ChessFormation.getTeamAt(coord);

            final ChessBoard barracksBoard = boardManager.getBarracksBoard(team);
            if (barracksBoard == null) continue;

            final Location targetLoc = barracksBoard.toCenterLocation(coord);
            final Vector direction = (team == Team.WHITE) ? barracksBoard.forwardVector() : new Vector(-barracksBoard.forwardVector().getX(), -barracksBoard.forwardVector().getY(), -barracksBoard.forwardVector().getZ());
            targetLoc.setDirection(direction);

            entity.teleport(targetLoc);
        }
    }

    public void deployBunkerToBattlefield(
            final ChessBoard board,
            final Collection<Participant> participants
    ) {
        final Map<Coordinate, Participant> participantMap = indexParticipants(participants);
        final Location reusableLoc = board.origin().clone();

        for (final Map.Entry<Coordinate, LivingEntity> entry : bunkerEntities.entrySet()) {
            final Coordinate coordinate = entry.getKey();
            final LivingEntity entity = entry.getValue();
            final Team team = ChessFormation.getTeamAt(coordinate);
            final Participant participant = participantMap.get(coordinate);

            final PieceType type = pdcMapper.readType(entity).orElse(PieceType.PAWN);
            final Piece piece = (participant != null)
                    ? Piece.of(participant.playerId(), team, type)
                    : Piece.of(null, team, type);

            if (participant != null) {
                attachAbilities(piece);
            }
            placePiece(coordinate, piece);

            if (participant == null) {
                board.updateToCenterLocation(coordinate, reusableLoc);
                final Vector direction = (team == Team.WHITE) ? board.forwardVector() : new Vector(-board.forwardVector().getX(), -board.forwardVector().getY(), -board.forwardVector().getZ());
                reusableLoc.setDirection(direction);

                entity.teleport(reusableLoc);
                registerPieceEntity(coordinate, entity);
            } else {
                // 플레이어가 차지할 자리는 벙커 기물을 숨기거나 제거
                entity.remove();
            }
        }
        bunkerEntities.clear();
    }

    private void clearBunker() {
        for (final LivingEntity entity : bunkerEntities.values()) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        bunkerEntities.clear();
    }

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

            if (participant != null) {
                attachAbilities(piece);
            }
            placePiece(coordinate, piece);

            if (participant == null) {
                board.updateToCenterLocation(coordinate, reusableLoc);
                spawnPiece(type, team, coordinate, reusableLoc, direction, false);
            }
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

        Bukkit.getPluginManager().callEvent(new PieceSpawnEvent(living, type, team));

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

        if (victimPiece != null) {
            // 피해자 엔티티 추적 명시적 해제 (이후 movePiece가 해당 좌표를 차지함)
            final LivingEntity victimEntity = pieceState.pieceEntities().get(victim);
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

        // 데스 이벤트가 아닌 단순 언로드/순간이동일 경우 보드에서 기물을 제거하지 않음
        if (!entity.isDead()) {
            return false;
        }

        final Optional<Coordinate> coordinateOpt = pdcMapper.readCoordinate(entity);

        if (coordinateOpt.isPresent()) {
            final Coordinate coordinate = coordinateOpt.get();
            final LivingEntity registeredEntity = pieceState.pieceEntities().get(coordinate);

            // 현재 사라진 엔티티가 실제로 보드에 등록된 해당 좌표의 엔티티가 맞는지 검증
            // (벙커 기물을 지울 때 플레이어 기물까지 지워지는 현상 방지)
            if (registeredEntity != null && registeredEntity.getUniqueId().equals(entity.getUniqueId())) {
                removePiece(coordinate);
            } else if (registeredEntity == null && pieceState.boardPieces().containsKey(coordinate) && !pieceState.boardPieces().get(coordinate).isPlayerPiece()) {
                // 플레이어 기물이 아닌데 등록된 엔티티가 없다면 벙커 초기화 등으로 인한 예외 케이스이므로 삭제
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

        final Piece piece = pieceState.boardPieces().get(from);
        if (piece == null) {
            return;
        }

        // 1. 목적지 위치 계산
        final Location mobTarget = board.updateToCenterLocation(to, board.origin().clone());
        final Vector direction = (piece.team() == Team.WHITE) ? board.forwardVector() : new Vector(-board.forwardVector().getX(), -board.forwardVector().getY(), -board.forwardVector().getZ());
        mobTarget.setDirection(direction);

        final Location playerTarget = mobTarget.clone().add(0, PLAYER_TELEPORT_OFFSET_Y, 0);

        // 2. 몹 순간이동 및 렌더링 글리치 방어
        final LivingEntity mobEntity = pieceState.pieceEntities().get(from);
        if (mobEntity != null) {
            toggleForceRender(mobEntity, true);
            mobEntity.teleport(mobTarget);

            // 순간이동 후 클라이언트 동기화를 위해 약간의 딜레이 후 렌더링 옵션 해제 권장 (스케줄러 필요)
            Bukkit.getScheduler().runTaskLater(plugin, () -> toggleForceRender(mobEntity, false), 2L);

            updateMobMapping(mobEntity, from, to);
        }

        // 3. 플레이어 순간이동
        if (piece.isPlayerPiece()) {
            final Player player = Bukkit.getPlayer(piece.ownerId());
            if (player != null) {
                player.teleport(playerTarget);
                updatePlayerMapping(player, to);
            }
        }

        // 4. 보드 데이터 상태 갱신
        pieceState.boardPieces().remove(from);
        pieceState.boardPieces().put(to, piece);
    }

    private void toggleForceRender(final LivingEntity entity, final boolean enabled) {
        // TODO: BetterModel API 연동 (v1.6.1+ smooth 옵션 및 tracker.forceUpdate 활용 권장)
        // 현재 클래스패스에 BetterModel API가 감지되지 않아 플레이스홀더로 유지함.
        // 예: BetterModelAPI.getTracker(entity).ifPresent(t -> t.setForceRender(enabled));
    }

    private void updateMobMapping(final LivingEntity entity, final Coordinate from, final Coordinate to) {
        pdcMapper.updateCoordinate(entity, to);
        pieceState.pieceEntities().remove(from);
        pieceState.pieceEntities().put(to, entity);
        pieceState.entityToCoordinate().put(entity.getUniqueId(), to);
    }

    private void updatePlayerMapping(final Player player, final Coordinate to) {
        pieceState.entityToCoordinate().put(player.getUniqueId(), to);
    }

    public Optional<Coordinate> findCoordinate(final org.bukkit.entity.Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }

        // 1. 메모리 매핑 우선 확인
        final Coordinate memoryCoord = pieceState.entityToCoordinate().get(entity.getUniqueId());
        if (memoryCoord != null) {
            return Optional.of(memoryCoord);
        }

        // 2. 메모리에 없을 경우(청크 언로드 후 재로드 등) PDC에서 확인 및 메모리 갱신
        final Optional<Coordinate> pdcCoord = pdcMapper.readCoordinate(entity);
        pdcCoord.ifPresent(coordinate -> {
            pieceState.entityToCoordinate().put(entity.getUniqueId(), coordinate);
            if (entity instanceof LivingEntity living) {
                pieceState.pieceEntities().put(coordinate, living);
            }
        });

        return pdcCoord;
    }

    public void registerPlayerPiece(final Player player, final Team team, final PieceType type, final Coordinate coordinate) {
        final Piece piece = Piece.of(player.getUniqueId(), team, type);
        attachAbilities(piece);
        placePiece(coordinate, piece);
    }

    public void attachAbilities(final Piece piece) {
        switch (piece.type()) {
            case ROOK -> piece.addAbility(new RookAbility(announcer));
            case KNIGHT -> piece.addAbility(new KnightAbility(pieceState, announcer));
            case BISHOP -> piece.addAbility(new BishopAbility(pieceState, announcer));
            case PAWN -> piece.addAbility(new PawnAbility());
            case QUEEN -> {
                piece.addAbility(new RookAbility(announcer));
                piece.addAbility(new BishopAbility(pieceState, announcer));
            }
            case KING -> piece.addAbility(new KingAbility(pieceState, combatPolicy, announcer));
        }
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
