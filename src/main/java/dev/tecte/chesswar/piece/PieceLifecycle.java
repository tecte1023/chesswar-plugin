package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PieceLifecycle {
    private final SimulationEventBus eventBus;
    private int nextId = 1;

    public void spawn(final int gridX, final int gridY) {
        var piece = new Entity(nextId++);
        piece.attach(GridComponent.class, new GridComponent(new GridPoint(gridX, gridY)));
        eventBus.publish(new PieceSpawnEvent(piece));
    }

    public void clear() {
        nextId = 1;
        eventBus.publish(new PieceClearEvent());
    }
}
