package dev.tecte.chessWar.board.domain.model.spec;

import lombok.NonNull;

/**
 * 체스판 격자의 행과 열 개수를 정의하는 레코드입니다.
 *
 * @param rowCount 격자의 행 개수
 * @param colCount 격자의 열 개수
 */
public record GridSpec(int rowCount, int colCount) {
    private static final int ROW_COUNT = 8;
    private static final int COL_COUNT = 8;

    public GridSpec {
        if (rowCount <= 0) {
            throw new IllegalArgumentException("Row must be positive.");
        }

        if (colCount <= 0) {
            throw new IllegalArgumentException("Column must be positive.");
        }
    }

    /**
     * 기본값으로 {@link GridSpec} 인스턴스를 생성합니다.
     *
     * @return 기본 격자 명세
     */
    @NonNull
    public static GridSpec defaultSpec() {
        return new GridSpec(ROW_COUNT, COL_COUNT);
    }
}
