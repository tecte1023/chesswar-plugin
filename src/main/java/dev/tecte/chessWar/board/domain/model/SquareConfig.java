package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;
import org.bukkit.Material;

@Builder
public record SquareConfig(
        int rowCount,
        int columnCount,
        int width,
        int height,
        Material blackBlock,
        Material whiteBlock
) {
}
