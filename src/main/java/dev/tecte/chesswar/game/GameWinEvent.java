package dev.tecte.chesswar.game;

import dev.tecte.chesswar.core.SimulationEvent;
import dev.tecte.chesswar.piece.Team;

public record GameWinEvent(Team winnerTeam) implements SimulationEvent {
}
