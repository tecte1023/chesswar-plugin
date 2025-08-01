package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;
import org.bukkit.util.Vector;

@Builder
public record Orientation(
        Vector forward,
        Vector left,
        Vector right
) {
}
