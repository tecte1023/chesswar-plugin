package dev.tecte.chessWar.board.domain.model;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Border(
        BoundingBox boundingBox,
        int thickness
) {
    @Contract("_, _ -> new")
    public static @NotNull Border from(@NotNull BoundingBox boundingBox, int thickness) {
        return new Border(boundingBox.clone().expand(thickness, 0, thickness), thickness);
    }
}
