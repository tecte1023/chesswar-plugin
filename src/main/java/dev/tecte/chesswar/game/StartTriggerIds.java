package dev.tecte.chesswar.game;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record StartTriggerIds(@NotNull UUID interactionId, @NotNull UUID textId) {
    public boolean matchesEntity(@NotNull final UUID entityId) {
        return interactionId.equals(entityId);
    }
}
