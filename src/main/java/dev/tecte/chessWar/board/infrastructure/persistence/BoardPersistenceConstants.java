package dev.tecte.chessWar.board.infrastructure.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 보드 기능의 영속성 계층에서 사용하는 상수들을 정의합니다.
 * YAML 파일의 경로와 키 이름들에 대한 단일 소스 역할을 합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoardPersistenceConstants {
    public static final String ROOT = "board";

    /**
     * YAML 파일이나 객체 매핑에 사용되는 순수 키 이름들을 정의합니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Keys {
        public static final String CONFIG = "config";
        public static final String STATE = "state";

        public static final String SQUARE = "square";
        public static final String INNER_BORDER = "inner-border";
        public static final String FRAME = "frame";

        public static final String ROW_COUNT = "row-count";
        public static final String COL_COUNT = "col-count";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String BLACK_BLOCK = "black-block";
        public static final String WHITE_BLOCK = "white-block";
        public static final String THICKNESS = "thickness";
        public static final String BLOCK = "block";

        public static final String SQUARE_GRID = "square-grid";
        public static final String ANCHOR = "anchor";
        public static final String ORIENTATION = "orientation";
        public static final String BOUNDING_BOX = "bounding-box";
    }

    /**
     * 설정 파일의 전체 경로들을 정의합니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ConfigPaths {
        private static final String CONFIG_PATH = ROOT + "." + Keys.CONFIG;

        public static final String SQUARE_PATH = CONFIG_PATH + "." + Keys.SQUARE;
        public static final String INNER_BORDER_PATH = CONFIG_PATH + "." + Keys.INNER_BORDER;
        public static final String FRAME_PATH = CONFIG_PATH + "." + Keys.FRAME;

        public static final String ROW_COUNT_PATH = SQUARE_PATH + "." + Keys.ROW_COUNT;
        public static final String COL_COUNT_PATH = SQUARE_PATH + "." + Keys.COL_COUNT;
        public static final String WIDTH_PATH = SQUARE_PATH + "." + Keys.WIDTH;
        public static final String HEIGHT_PATH = SQUARE_PATH + "." + Keys.HEIGHT;
        public static final String BLACK_BLOCK_PATH = SQUARE_PATH + "." + Keys.BLACK_BLOCK;
        public static final String WHITE_BLOCK_PATH = SQUARE_PATH + "." + Keys.WHITE_BLOCK;

        public static final String INNER_BORDER_THICKNESS_PATH = INNER_BORDER_PATH + "." + Keys.THICKNESS;
        public static final String INNER_BORDER_BLOCK_PATH = INNER_BORDER_PATH + "." + Keys.BLOCK;

        public static final String FRAME_THICKNESS_PATH = FRAME_PATH + "." + Keys.THICKNESS;
        public static final String FRAME_BLOCK_PATH = FRAME_PATH + "." + Keys.BLOCK;
    }

    /**
     * 상태 파일의 전체 경로들을 정의합니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class StatePaths {
        public static final String STATE_PATH = ROOT + "." + Keys.STATE;
    }
}
