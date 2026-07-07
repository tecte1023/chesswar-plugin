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
    public static final int BOARD_SIZE = 8;
    public static final int SQUARE_COUNT = BOARD_SIZE * BOARD_SIZE;

    private static final Coordinate[] CACHE = new Coordinate[SQUARE_COUNT];

    static {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                CACHE[flatten(x, y)] = new Coordinate(x, y);
            }
        }
    }

    private final int x;
    private final int y;

    @NotNull
    public static Coordinate of(final int x, final int y) {
        return CACHE[flatten(x, y)];
    }

    public static boolean isValid(final int x, final int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public static boolean isOutOfBounds(final int x, final int y) {
        return !isValid(x, y);
    }

    public static int flatten(final int x, final int y) {
        return x + y * BOARD_SIZE;
    }

    public int flatIndex() {
        return flatten(x, y);
    }

    public boolean isDarkSquare() {
        return (x + y) % 2 == 0;
    }

    public boolean isLightSquare() {
        return !isDarkSquare();
    }
}
