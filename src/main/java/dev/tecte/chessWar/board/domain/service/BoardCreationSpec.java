package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.BorderConfig;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import lombok.Builder;
import org.bukkit.util.Vector;

@Builder
public record BoardCreationSpec(
        Vector gridAnchor,
        Orientation orientation,
        SquareConfig squareConfig,
        BorderConfig innerBorderConfig,
        BorderConfig frameConfig
) {
}
