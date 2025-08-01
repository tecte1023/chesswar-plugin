package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;
import org.bukkit.util.Vector;

@Builder
public record Orientation(
        Vector up,
        Vector forward,
        Vector left,
        Vector right
) {
}
