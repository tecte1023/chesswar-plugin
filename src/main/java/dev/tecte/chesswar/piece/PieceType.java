package dev.tecte.chesswar.piece;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PieceType {
    KING("킹", 200, 5),
    QUEEN("퀸", 100, 25),
    ROOK("룩", 150, 15),
    BISHOP("비숍", 80, 20),
    KNIGHT("나이트", 100, 20);

    private final String displayName;
    private final double baseHp;
    private final double baseDamage;
}
