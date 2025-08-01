package dev.tecte.chessWar.board.domain.model;

import org.bukkit.util.BoundingBox;

public record Border(
        BoundingBox boundingBox,
        int thickness
) {
}
