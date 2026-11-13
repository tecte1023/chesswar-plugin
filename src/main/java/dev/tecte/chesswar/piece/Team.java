package dev.tecte.chesswar.piece;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Team {
    WHITE(0, 1, 1),
    BLACK(7, 6, -1);

    private final int backRank;
    private final int pawnRank;
    private final int forwardStep;
}
