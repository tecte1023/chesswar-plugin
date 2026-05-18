package dev.tecte.chesswar.board;

public record Coordinate(int x, int y) {
    public static Coordinate of(int x, int y) {
        return new Coordinate(x, y);
    }

    public boolean isValid() {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }
}
