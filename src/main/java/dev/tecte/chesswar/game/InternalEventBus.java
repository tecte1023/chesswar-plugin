package dev.tecte.chesswar.game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class InternalEventBus {
    private final List<GamePhaseChangeListener> phaseChangeListeners = new ArrayList<>();

    public void registerPhaseListener(@NotNull final GamePhaseChangeListener listener) {
        phaseChangeListeners.add(listener);
    }

    public void publishPhaseChange(@NotNull final GamePhase newPhase) {
        for (final GamePhaseChangeListener listener : phaseChangeListeners) {
            listener.onPhaseChanged(newPhase);
        }
    }
}
