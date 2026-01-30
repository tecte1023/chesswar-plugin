package dev.tecte.chessWar.game.infrastructure.persistence;

import lombok.experimental.UtilityClass;

/**
 * 게임 영속성 상수 집합입니다.
 */
@UtilityClass
public class GamePersistenceConstants {
    public final String ROOT = "game";

    /**
     * YAML 매핑 키 상수입니다.
     */
    @UtilityClass
    public class Keys {
        public final String STATE = "state";
        public final String PIECES = "pieces";
        public final String PHASE = "phase";
        public final String CURRENT = "current";
        public final String SELECTIONS = "selections";
    }

    /**
     * 상태 경로 상수입니다.
     */
    @UtilityClass
    public class StatePaths {
        public final String STATE_PATH = ROOT + "." + Keys.STATE;
    }
}
