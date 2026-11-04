package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.core.Component;

import java.util.UUID;

public record PlayerComponent(UUID playerId) implements Component {
}
