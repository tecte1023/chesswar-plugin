package dev.tecte.chessWar.board.domain.model;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public record Border(BoundingBox boundingBox, int thickness) {
    @NotNull
    public static Border from(@NotNull BoundingBox inside, int thickness) {
        BoundingBox expandedBox = inside.clone().expand(thickness, 0, thickness);

        return new Border(expandedBox, thickness);
    }
}
