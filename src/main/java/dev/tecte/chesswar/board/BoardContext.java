package dev.tecte.chesswar.board;

import org.bukkit.util.Vector;

import java.util.UUID;

public final class BoardContext {
    private BoardState state;

    public void setup(final UUID worldId, final Vector playerWorldPos, final float playerYaw) {
        state = BoardState.create(worldId, playerWorldPos, playerYaw);
    }

    public void applyWorldPos(final Vector targetWorldPos, final GridPoint gridPos) {
        state.applyWorldPos(targetWorldPos, gridPos);
    }

    public UUID worldId() {
        return state.worldId();
    }
}
