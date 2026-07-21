package dev.tecte.chesswar.game;

import org.jetbrains.annotations.NotNull;

public interface GamePhaseChangeListener {
    void onPhaseChanged(@NotNull final GamePhase newPhase);
}
