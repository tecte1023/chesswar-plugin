package dev.tecte.chessWar.game.infrastructure.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 게임 데이터의 영속성 처리와 관련된 상수들을 정의하는 클래스입니다.
 * YML 파일의 키 이름이나 경로 등을 관리합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GamePersistenceConstants {
    public static final String ROOT = "game";

    /**
     * YAML 파일이나 객체 매핑에 사용되는 순수 키 이름들을 정의합니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Keys {
        public static final String STATE = "state";

        public static final String PIECES = "pieces";
        public static final String PHASE = "phase";
        public static final String CURRENT_TURN = "currentTurn";

        public static final String ENTITY_ID = "entityId";
        public static final String PIECE_TYPE = "type";
        public static final String TEAM_COLOR = "team";
        public static final String MOB_ID = "mobId";
    }

    /**
     * 상태 파일의 전체 경로들을 정의합니다.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class StatePaths {
        public static final String STATE_PATH = ROOT + "." + Keys.STATE;
    }
}
