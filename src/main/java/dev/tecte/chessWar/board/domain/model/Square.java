package dev.tecte.chessWar.board.domain.model;

import org.bukkit.util.BoundingBox;

public record Square(BoundingBox boundingBox, SquareColor color) {
}
