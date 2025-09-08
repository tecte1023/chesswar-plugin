package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record Board(
        UUID id,
        SquareGrid squareGrid,
        Border innerBorder,
        Border frame
) {
}
