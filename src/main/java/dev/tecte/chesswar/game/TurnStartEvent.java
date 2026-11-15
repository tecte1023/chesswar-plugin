package dev.tecte.chesswar.game;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEvent;

public record TurnStartEvent(Entity piece) implements SimulationEvent {
}
