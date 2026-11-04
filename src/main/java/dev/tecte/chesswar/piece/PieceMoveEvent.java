package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEvent;

public record PieceMoveEvent(Entity piece) implements SimulationEvent {
}
