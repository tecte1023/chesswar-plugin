package dev.tecte.chesswar.board;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.UUID;

public final class BoardContext {
    private BoardState state;

    public void setup(final Location loc) {
        state = BoardState.create(loc);
    }

    public Vector toWorldPos(final GridPoint gridPos) {
        return state.toWorldPos(gridPos);
    }

    public GridPoint toGridPos(final Vector worldPos) {
        return state.toGridPos(worldPos);
    }

    public boolean isSetup() {
        return state != null;
    }

    public boolean isInBounds(final GridPoint gridPos) {
        return state.isInBounds(gridPos);
    }

    public UUID worldId() {
        return state.worldId();
    }

    public float yaw() {
        return state.yaw();
    }
}
