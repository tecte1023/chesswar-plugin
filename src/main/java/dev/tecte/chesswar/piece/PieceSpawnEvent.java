package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEvent;

public record PieceSpawnEvent(Entity piece) implements SimulationEvent {
}
