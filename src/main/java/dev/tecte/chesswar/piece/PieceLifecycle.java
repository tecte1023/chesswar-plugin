package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEventBus;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class PieceLifecycle {
    private static final PieceType[] BACK_RANK_PIECES = {
            PieceType.ROOK,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.QUEEN,
            PieceType.KING,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK
    };

    private final SimulationEventBus eventBus;
    private final Map<GridPoint, Entity> piecesByGridPos = new HashMap<>();
    private int nextId = 1;

    public SpawnResult spawn(final GridPoint gridPos, final Team team, final PieceType type) {
        if (piecesByGridPos.containsKey(gridPos)) {
            return SpawnResult.ALREADY_OCCUPIED;
        }

        Entity piece = createPiece(gridPos, team, type);
        eventBus.publish(new PieceSpawnEvent(piece));

        return SpawnResult.SUCCESS;
    }

    public PieceSetupResult setupPieces() {
        if (!piecesByGridPos.isEmpty()) {
            return PieceSetupResult.ALREADY_INITIALIZED;
        }

        List<Entity> createdPieces = new ArrayList<>(32);

        for (int x = 0; x < 8; x++) {
            createdPieces.add(createPiece(new GridPoint(x, Team.WHITE.backRank()), Team.WHITE, BACK_RANK_PIECES[x]));
            createdPieces.add(createPiece(new GridPoint(x, Team.WHITE.pawnRank()), Team.WHITE, PieceType.PAWN));
            createdPieces.add(createPiece(new GridPoint(x, Team.BLACK.backRank()), Team.BLACK, BACK_RANK_PIECES[x]));
            createdPieces.add(createPiece(new GridPoint(x, Team.BLACK.pawnRank()), Team.BLACK, PieceType.PAWN));
        }

        eventBus.publish(new PiecesSpawnEvent(createdPieces));

        return PieceSetupResult.SUCCESS;
    }

    public BindResult bindPlayer(final GridPoint gridPos, final UUID playerId) {
        if (pieceOf(playerId) != null) {
            return BindResult.PLAYER_ALREADY_BOUND;
        }

        Entity piece = pieceAt(gridPos);
        if (piece == null) {
            return BindResult.PIECE_NOT_FOUND;
        }

        if (piece.hasComponent(PlayerComponent.class)) {
            return BindResult.ALREADY_BOUND;
        }

        piece.attach(PlayerComponent.class, new PlayerComponent(playerId));
        eventBus.publish(new PieceBindEvent(piece));

        return BindResult.SUCCESS;
    }

    public void clear() {
        piecesByGridPos.clear();
        nextId = 1;
        eventBus.publish(new PieceClearEvent());
    }

    public MoveResult move(final Entity piece, final GridPoint targetGridPos) {
        var gridComponent = piece.component(GridComponent.class);
        GridPoint currentPos = gridComponent.gridPos();

        var pieceComponent = piece.component(PieceComponent.class);
        if (!pieceComponent.canMoveTo(currentPos, targetGridPos)) {
            return MoveResult.INVALID_PATTERN;
        }

        if (piecesByGridPos.containsKey(targetGridPos)) {
            return MoveResult.ALREADY_OCCUPIED;
        }

        if (!pieceComponent.canJump() && !isPathClear(currentPos, targetGridPos)) {
            return MoveResult.PATH_BLOCKED;
        }

        relocate(piece, targetGridPos);

        return MoveResult.SUCCESS;
    }

    public AttackResult attack(final Entity target, final Entity attacker) {
        var attackerPiece = attacker.component(PieceComponent.class);
        var targetPiece = target.component(PieceComponent.class);
        if (attackerPiece.isAlly(targetPiece)) {
            return AttackResult.Failure.FRIENDLY_FIRE;
        }

        GridPoint attackerPos = attacker.component(GridComponent.class).gridPos();
        GridPoint targetPos = target.component(GridComponent.class).gridPos();

        if (!attackerPiece.canAttackTo(attackerPos, targetPos)) {
            return AttackResult.Failure.INVALID_PATTERN;
        }

        if (!attackerPiece.canJump() && !isPathClear(attackerPos, targetPos)) {
            return AttackResult.Failure.PATH_BLOCKED;
        }

        var attackerCombat = attacker.component(CombatComponent.class);
        var targetCombat = target.component(CombatComponent.class);
        int damage = attackerCombat.atk();
        targetCombat.applyDamage(damage);

        if (targetCombat.isDead()) {
            piecesByGridPos.remove(targetPos);
            eventBus.publish(new PieceDeathEvent(target));
            relocate(attacker, targetPos);
        }

        return new AttackResult.Success(damage);
    }

    public Entity pieceAt(final GridPoint gridPos) {
        return piecesByGridPos.get(gridPos);
    }

    public Entity pieceOf(final UUID playerId) {
        for (Entity piece : piecesByGridPos.values()) {
            if (!piece.hasComponent(PlayerComponent.class)) {
                continue;
            }

            if (piece.component(PlayerComponent.class).playerId().equals(playerId)) {
                return piece;
            }
        }

        return null;
    }

    private Entity createPiece(final GridPoint gridPos, final Team team, final PieceType type) {
        var piece = new Entity(nextId++);
        piece.attach(GridComponent.class, new GridComponent(gridPos));
        piece.attach(PieceComponent.class, new PieceComponent(team, type));
        piece.attach(CombatComponent.class, new CombatComponent(type.maxHp(), type.atk()));
        piecesByGridPos.put(gridPos, piece);

        return piece;
    }

    private void relocate(final Entity piece, final GridPoint targetGridPos) {
        var gridComponent = piece.component(GridComponent.class);
        piecesByGridPos.remove(gridComponent.gridPos());
        gridComponent.moveTo(targetGridPos);
        piecesByGridPos.put(targetGridPos, piece);

        eventBus.publish(new PieceMoveEvent(piece));
    }

    private boolean isPathClear(final GridPoint from, final GridPoint to) {
        int dx = to.x() - from.x();
        int dy = to.y() - from.y();
        int stepX = Integer.signum(dx);
        int stepY = Integer.signum(dy);

        int currentX = from.x() + stepX;
        int currentY = from.y() + stepY;

        while (currentX != to.x() || currentY != to.y()) {
            if (piecesByGridPos.containsKey(new GridPoint(currentX, currentY))) {
                return false;
            }

            currentX += stepX;
            currentY += stepY;
        }

        return true;
    }
}
