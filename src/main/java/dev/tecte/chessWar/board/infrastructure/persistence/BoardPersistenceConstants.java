package dev.tecte.chessWar.board.infrastructure.persistence;

import lombok.experimental.UtilityClass;

/**
 * 보드 기능의 영속성 계층에서 사용하는 상수들을 정의합니다.
 * YAML 파일의 경로와 키 이름들에 대한 단일 소스 역할을 합니다.
 */
@UtilityClass
public class BoardPersistenceConstants {
    public final String ROOT = "board";

    /**
     * YAML 파일이나 객체 매핑에 사용되는 순수 키 이름들을 정의합니다.
     */
    @UtilityClass
    public static class Keys {
        public final String STATE = "state";

        public final String WORLD_NAME = "world";
        public final String SQUARE_GRID = "square-grid";
        public final String INNER_BORDER = "inner-border";
        public final String FRAME = "frame";

        public final String ANCHOR = "anchor";
        public final String ORIENTATION = "orientation";
        public final String BOUNDING_BOX = "bounding-box";
    }

    /**
     * 상태 파일의 전체 경로들을 정의합니다.
     */
    @UtilityClass
    public static class StatePaths {
        public final String STATE_PATH = ROOT + "." + Keys.STATE;
    }
}
