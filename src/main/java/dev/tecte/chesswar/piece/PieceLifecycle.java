package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEventBus;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class PieceLifecycle {
    private static final int DEFAULT_PAWN_HP = 60;
    private static final int DEFAULT_PAWN_ATK = 15;

    private final SimulationEventBus eventBus;
    private final Map<GridPoint, Entity> piecesByGridPos = new HashMap<>();
    private int nextId = 1;

    public void spawn(final GridPoint gridPos) {
        Entity piece = createPiece(gridPos);
        eventBus.publish(new PieceSpawnEvent(piece));
    }

    private Entity createPiece(final GridPoint gridPos) {
        var piece = new Entity(nextId++);
        piece.attach(GridComponent.class, new GridComponent(gridPos));
        piece.attach(CombatComponent.class, new CombatComponent(DEFAULT_PAWN_HP, DEFAULT_PAWN_ATK));
        piecesByGridPos.put(gridPos, piece);

        return piece;
    }

    public void bindPlayer(final Entity piece, final UUID playerId) {
        piece.attach(PlayerComponent.class, new PlayerComponent(playerId));
        eventBus.publish(new PieceBindEvent(piece));
    }

    public void clear() {
        piecesByGridPos.clear();
        nextId = 1;
        eventBus.publish(new PieceClearEvent());
    }

    public void move(final Entity piece, final GridPoint targetGridPos) {
        var gridComponent = piece.component(GridComponent.class);

        piecesByGridPos.remove(gridComponent.gridPos());
        gridComponent.moveTo(targetGridPos);
        piecesByGridPos.put(targetGridPos, piece);

        eventBus.publish(new PieceMoveEvent(piece));
    }

    public int attack(final Entity target, final Entity attacker) {
        var attackerCombat = attacker.component(CombatComponent.class);
        var targetCombat = target.component(CombatComponent.class);
        int damage = attackerCombat.atk();
        targetCombat.applyDamage(damage);

        if (targetCombat.isDead()) {
            GridPoint targetGridPos = target.component(GridComponent.class).gridPos();
            piecesByGridPos.remove(targetGridPos);
            eventBus.publish(new PieceDeathEvent(target));
            move(attacker, targetGridPos);
        }

        return damage;
    }

    public boolean hasPieceAt(final GridPoint gridPos) {
        return piecesByGridPos.containsKey(gridPos);
    }

    public Entity pieceAt(final GridPoint gridPos) {
        return piecesByGridPos.get(gridPos);
    }

    public boolean hasPlayerPiece(final UUID playerId) {
        return pieceOf(playerId) != null;
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
}
