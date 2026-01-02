package dev.tecte.chessWar.piece.infrastructure.persistence;

import lombok.experimental.UtilityClass;

/**
 * 기물 영속성 계층에서 사용되는 상수들을 정의합니다.
 */
@UtilityClass
public class PiecePersistenceConstants {
    /**
     * YAML 파일이나 객체 매핑에 사용되는 순수 키 이름들을 정의합니다.
     */
    @UtilityClass
    public static class Keys {
        public static final String ENTITY_ID = "entity-id";
        public static final String PIECE_TYPE = "type";
        public static final String TEAM_COLOR = "team";
        public static final String PLAYER_ID = "player-id";
    }
}
