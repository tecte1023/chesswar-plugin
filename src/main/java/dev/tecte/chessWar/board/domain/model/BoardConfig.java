package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;

@Builder
public record BoardConfig(
        SquareConfig squareConfig,
        BorderConfig innerBorderConfig,
        BorderConfig frameConfig
) {
}
