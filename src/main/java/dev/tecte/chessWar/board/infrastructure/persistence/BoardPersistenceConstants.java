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
        public static final String STATE = "state";

        public static final String INNER_BORDER = "inner-border";
        public static final String FRAME = "frame";
        public static final String SQUARE_GRID = "square-grid";

        public static final String ANCHOR = "anchor";
        public static final String ORIENTATION = "orientation";
        public static final String BOUNDING_BOX = "bounding-box";
    }

    /**
     * 상태 파일의 전체 경로들을 정의합니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class StatePaths {
        public static final String STATE_PATH = ROOT + "." + Keys.STATE;
    }
}
