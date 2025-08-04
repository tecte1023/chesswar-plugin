package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;
import org.bukkit.util.BoundingBox;

@Builder
public record Square(
        double logicalRow,
        double logicalCol,
        BoundingBox boundingBox,
        SquareColor color
) {
}
