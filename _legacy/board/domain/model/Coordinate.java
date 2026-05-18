package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;

/**
 * 체스판 위의 위치를 나타내는 좌표입니다.
 *
 * @param row 행 인덱스 (0-7, 대응되는 Rank: 1-8)
 * @param col 열 인덱스 (0-7, 대응되는 File: A-H)
 */
public record Coordinate(int row, int col) {
    public static final int NOTATION_LENGTH = 2;
    public static final String NOTATION_RANGE = "A1-H8";

    public Coordinate {
        if (row < 0 || row > 7) {
            throw new IllegalArgumentException("Row must be between 0 and 7.");
        }

        if (col < 0 || col > 7) {
            throw new IllegalArgumentException("Column must be between 0 and 7.");
        }
    }

    /**
     * 기보 문자열로부터 좌표를 생성합니다.
     *
     * @param notation 기보 문자열 (예: "A1")
     * @return 좌표
     * @throws IllegalArgumentException 형식이 올바르지 않은 경우
     */
    @NonNull
    public static Coordinate from(@NonNull String notation) {
        if (notation.length() != NOTATION_LENGTH) {
            throw new IllegalArgumentException("Notation must be exactly " + NOTATION_LENGTH + " characters long.");
        }

        int col = Character.toUpperCase(notation.charAt(0)) - 'A';
        int row = notation.charAt(1) - '1';

        return new Coordinate(row, col);
    }

    /**
     * 좌표를 대수 기보법으로 변환합니다.
     *
     * @return 기보 문자열 (예: "A1")
     */
    @NonNull
    public String toNotation() {
        char file = (char) ('A' + col);
        char rank = (char) ('1' + row);

        return "" + file + rank;
    }
}
