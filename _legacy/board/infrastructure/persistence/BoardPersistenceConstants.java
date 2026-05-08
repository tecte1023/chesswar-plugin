package dev.tecte.chessWar.board.infrastructure.persistence;

import lombok.experimental.UtilityClass;

/**
 * 체스판 영속성 상수 집합입니다.
 */
@UtilityClass
public class BoardPersistenceConstants {
    public final String ROOT = "board";

    /**
     * YAML 매핑 키 상수입니다.
     */
    @UtilityClass
    public class Keys {
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
     * 상태 경로 상수입니다.
     */
    @UtilityClass
    public class StatePaths {
        public final String STATE_PATH = ROOT + "." + Keys.STATE;
    }
}
