package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;

@Builder
public record Board(
        SquareGrid squares,
        Border innerBorder,
        Border frame
) {
}
