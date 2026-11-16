package dev.tecte.chesswar.game;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEvent;

import java.util.List;

public record GameStartEvent(List<Entity> pieces) implements SimulationEvent {
}
