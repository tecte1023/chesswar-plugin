package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.util.BoundingBox;

public record Border(BoundingBox boundingBox, int thickness) {
    @NonNull
    public static Border from(@NonNull BoundingBox inside, int thickness) {
        BoundingBox expandedBox = inside.clone().expand(thickness, 0, thickness);

        return new Border(expandedBox, thickness);
    }
}
