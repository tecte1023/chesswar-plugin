package dev.tecte.chesswar.board;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Value
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Coordinate {
    private static final Coordinate[] CACHE = new Coordinate[ChessFormation.BOARD_SIZE * ChessFormation.BOARD_SIZE];

    int x;
    int y;

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
