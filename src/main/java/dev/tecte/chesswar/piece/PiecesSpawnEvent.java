package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEvent;

import java.util.List;

public record PiecesSpawnEvent(List<Entity> pieces) implements SimulationEvent {
}
