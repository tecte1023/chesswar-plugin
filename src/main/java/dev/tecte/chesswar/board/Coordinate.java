package dev.tecte.chesswar.board;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Coordinate {
    private static final Coordinate[] CACHE = new Coordinate[ChessFormation.BOARD_SIZE * ChessFormation.BOARD_SIZE];

    private final int x;
    private final int y;

    static {
        for (int i = 0; i < ChessFormation.BOARD_SIZE; i++) {
            for (int j = 0; j < ChessFormation.BOARD_SIZE; j++) {
                CACHE[i * ChessFormation.BOARD_SIZE + j] = new Coordinate(i, j);
            }
        }
    }

    @NotNull
    public static Coordinate of(final int x, final int y) {
        if (isOutOfBounds(x, y)) {
            throw new IllegalArgumentException("Invalid coordinate: x=%d, y=%d".formatted(x, y));
        }

        return CACHE[x * ChessFormation.BOARD_SIZE + y];
    }

    private static boolean isOutOfBounds(final int x, final int y) {
        return x < 0 || x >= ChessFormation.BOARD_SIZE || y < 0 || y >= ChessFormation.BOARD_SIZE;
    }
}
