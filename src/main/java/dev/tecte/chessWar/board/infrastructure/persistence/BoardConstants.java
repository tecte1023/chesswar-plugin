package dev.tecte.chessWar.board.infrastructure.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;

/**
 * 체스판 모듈에서 사용하는 상수들을 정의하는 클래스입니다.
 * 설정 파일의 경로, 기본값, 유효성 검사 범위 등을 포함합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoardConstants {
    public static final String ROOT = "board";

    /**
     * 정적 설정과 관련된 상수들입니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Config {
        public static final String CONFIG_PATH = ROOT + ".config";

        public static final String ROW_COUNT = "row-count";
        public static final String COL_COUNT = "col-count";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String BLACK_BLOCK = "black-block";
        public static final String WHITE_BLOCK = "white-block";

        public static final String SQUARE_PATH = CONFIG_PATH + ".square";
        public static final String ROW_COUNT_PATH = SQUARE_PATH + "." + ROW_COUNT;
        public static final String COL_COUNT_PATH = SQUARE_PATH + "." + COL_COUNT;
        public static final String WIDTH_PATH = SQUARE_PATH + "." + WIDTH;
        public static final String HEIGHT_PATH = SQUARE_PATH + "." + HEIGHT;
        public static final String BLACK_BLOCK_PATH = SQUARE_PATH + "." + BLACK_BLOCK;
        public static final String WHITE_BLOCK_PATH = SQUARE_PATH + "." + WHITE_BLOCK;

        public static final String THICKNESS = "thickness";
        public static final String BLOCK = "block";

        public static final String INNER_BORDER_PATH = CONFIG_PATH + ".inner-border";
        public static final String INNER_BORDER_THICKNESS_PATH = INNER_BORDER_PATH + "." + THICKNESS;
        public static final String INNER_BORDER_BLOCK_PATH = INNER_BORDER_PATH + "." + BLOCK;

        public static final String FRAME_PATH = CONFIG_PATH + ".frame";
        public static final String FRAME_THICKNESS_PATH = FRAME_PATH + "." + THICKNESS;
        public static final String FRAME_BLOCK_PATH = FRAME_PATH + "." + BLOCK;
    }

    /**
     * 설정 값이 유효하지 않거나 없을 경우 사용될 기본값들입니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Defaults {
        public static final int DEFAULT_ROW_COUNT = 8;
        public static final int DEFAULT_COL_COUNT = 8;
        public static final int DEFAULT_SQUARE_WIDTH = 3;
        public static final int DEFAULT_SQUARE_HEIGHT = 3;
        public static final Material DEFAULT_SQUARE_BLACK_BLOCK = Material.STRIPPED_DARK_OAK_WOOD;
        public static final Material DEFAULT_SQUARE_WHITE_BLOCK = Material.STRIPPED_BIRCH_WOOD;
        public static final int DEFAULT_INNER_BORDER_THICKNESS = 1;
        public static final Material DEFAULT_INNER_BORDER_BLOCK = Material.STRIPPED_OAK_WOOD;
        public static final int DEFAULT_FRAME_THICKNESS = 2;
        public static final Material DEFAULT_FRAME_BLOCK = Material.DARK_OAK_WOOD;

        public static final int MIN_ROW_COUNT = 1;
        public static final int MAX_ROW_COUNT = 16;
        public static final int MIN_COL_COUNT = 1;
        public static final int MAX_COL_COUNT = 16;
        public static final int MIN_SQUARE_WIDTH = 1;
        public static final int MAX_SQUARE_WIDTH = 7;
        public static final int MIN_SQUARE_HEIGHT = 1;
        public static final int MAX_SQUARE_HEIGHT = 7;
        public static final int MIN_INNER_BORDER_THICKNESS = 0;
        public static final int MAX_INNER_BORDER_THICKNESS = 3;
        public static final int MIN_FRAME_THICKNESS = 0;
        public static final int MAX_FRAME_THICKNESS = 6;
    }

    /**
     * 동적 상태와 관련된 상수들입니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class State {
        public static final String STATE_PATH = ROOT + ".state";

        public static final String ID = "id";
        public static final String SQUARE_GRID = "square-grid";
        public static final String INNER_BORDER = "inner-border";
        public static final String FRAME = "frame";

        public static final String ANCHOR = "anchor";
        public static final String ORIENTATION = "orientation";

        public static final String BOUNDING_BOX = "bounding-box";
    }
}
