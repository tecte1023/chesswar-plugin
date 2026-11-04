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
