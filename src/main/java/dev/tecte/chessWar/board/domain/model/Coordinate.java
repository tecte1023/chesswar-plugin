package dev.tecte.chessWar.board.domain.model;

/**
 * 체스판의 좌표(행, 열)를 나타내는 불변 객체입니다.
 *
 * @param row 행 (0-7)
 * @param col 열 (0-7)
 */
public record Coordinate(int row, int col) {
    public Coordinate {
        if (row < 0 || row > 7) {
            throw new IllegalArgumentException("Row must be between 0 and 7.");
        }

        if (col < 0 || col > 7) {
            throw new IllegalArgumentException("Column must be between 0 and 7.");
        }
    }
}
