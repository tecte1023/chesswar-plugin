package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEvent;

public record PieceBindEvent(Entity piece) implements SimulationEvent {
}
