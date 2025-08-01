package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;

@Builder
public record Board(
        Square[][] squares,
        Border innerBorder,
        Border frame
) {
}
