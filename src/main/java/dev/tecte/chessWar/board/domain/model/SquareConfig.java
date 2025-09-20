package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.Material;

import java.util.Objects;

/**
 * 체스판 격자 칸의 정적 설정 정보를 담는 데이터 객체입니다.
 *
 * @param rowCount   행 수
 * @param colCount   열 수
 * @param width      칸의 너비
 * @param height     칸의 높이
 * @param blackBlock 검은색 칸을 구성하는 블록 종류
 * @param whiteBlock 흰색 칸을 구성하는 블록 종류
 */
public record SquareConfig(
        int rowCount,
        int colCount,
        int width,
        int height,
        Material blackBlock,
        Material whiteBlock
) {
    public static final int MIN_ROW_COUNT = 1;
    public static final int MAX_ROW_COUNT = 16;
    public static final int MIN_COL_COUNT = 1;
    public static final int MAX_COL_COUNT = 16;
    public static final int MIN_WIDTH = 1;
    public static final int MAX_WIDTH = 7;
    public static final int MIN_HEIGHT = 1;
    public static final int MAX_HEIGHT = 7;

    public static final int DEFAULT_ROW_COUNT = 8;
    public static final int DEFAULT_COL_COUNT = 8;
    public static final int DEFAULT_WIDTH = 3;
    public static final int DEFAULT_HEIGHT = 3;
    public static final Material DEFAULT_BLACK_BLOCK = Material.STRIPPED_DARK_OAK_WOOD;
    public static final Material DEFAULT_WHITE_BLOCK = Material.STRIPPED_BIRCH_WOOD;
    private static final SquareConfig DEFAULT = new SquareConfig(
            DEFAULT_ROW_COUNT,
            DEFAULT_COL_COUNT,
            DEFAULT_WIDTH,
            DEFAULT_HEIGHT,
            DEFAULT_BLACK_BLOCK,
            DEFAULT_WHITE_BLOCK
    );

    public SquareConfig {
        Objects.requireNonNull(blackBlock, "blackBlock must not be null");
        Objects.requireNonNull(whiteBlock, "whiteBlock must not be null");

        if (!isValidRowCount(rowCount)) {
            throw new IllegalArgumentException("Invalid row count: " + rowCount + ". Value must be between " + MIN_ROW_COUNT + " and " + MAX_ROW_COUNT + ".");
        }

        if (!isValidColCount(colCount)) {
            throw new IllegalArgumentException("Invalid column count: " + colCount + ". Value must be between " + MIN_COL_COUNT + " and " + MAX_COL_COUNT + ".");
        }

        if (!isValidWidth(width)) {
            throw new IllegalArgumentException("Invalid width: " + width + ". Value must be between " + MIN_WIDTH + " and " + MAX_WIDTH + ".");
        }

        if (!isValidHeight(height)) {
            throw new IllegalArgumentException("Invalid height: " + height + ". Value must be between " + MIN_HEIGHT + " and " + MAX_HEIGHT + ".");
        }

        if (!blackBlock.isBlock()) {
            throw new IllegalArgumentException("Black block must be a placeable block, but was " + blackBlock + ".");
        }

        if (!whiteBlock.isBlock()) {
            throw new IllegalArgumentException("White block must be a placeable block, but was " + whiteBlock + ".");
        }
    }

    /**
     * 기본값으로 채워진 {@link SquareConfig} 인스턴스를 반환합니다.
     *
     * @return 기본 설정 값을 가진 {@link SquareConfig} 객체
     */
    @NonNull
    public static SquareConfig ofDefault() {
        return DEFAULT;
    }

    /**
     * 주어진 행의 개수가 유효한 범위 내에 있는지 확인합니다.
     *
     * @param rowCount 검사할 행의 개수
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidRowCount(int rowCount) {
        return rowCount >= MIN_ROW_COUNT && rowCount <= MAX_ROW_COUNT;
    }

    /**
     * 주어진 열의 개수가 유효한 범위 내에 있는지 확인합니다.
     *
     * @param colCount 검사할 열의 개수
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidColCount(int colCount) {
        return colCount >= MIN_COL_COUNT && colCount <= MAX_COL_COUNT;
    }

    /**
     * 주어진 칸의 너비가 유효한 범위 내에 있는지 확인합니다.
     *
     * @param width 검사할 너비
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidWidth(int width) {
        return width >= MIN_WIDTH && width <= MAX_WIDTH;
    }

    /**
     * 주어진 칸의 높이가 유효한 범위 내에 있는지 확인합니다.
     *
     * @param height 검사할 높이
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidHeight(int height) {
        return height >= MIN_HEIGHT && height <= MAX_HEIGHT;
    }
}
