package dev.tecte.chesswar.board;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.UUID;

public final class BoardContext {
    private BoardState state;

    public void setup(final Location loc) {
        state = BoardState.create(loc);
    }

    public void applyWorldPos(final Vector targetWorldPos, final GridPoint gridPos) {
        state.applyWorldPos(targetWorldPos, gridPos);
    }

    public Vector toWorldPos(final GridPoint gridPos) {
        return state.toWorldPos(gridPos);
    }

    public boolean isSetup() {
        return state != null;
    }

    public boolean isInBounds(final int gridX, final int gridY) {
        return state.isInBounds(gridX, gridY);
    }

    public UUID worldId() {
        return state.worldId();
    }

    public float yaw() {
        return state.yaw();
    }
}
