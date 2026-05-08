package dev.tecte.chessWar.piece.infrastructure.persistence;

import lombok.experimental.UtilityClass;

/**
 * 기물 영속성 상수 집합입니다.
 */
@UtilityClass
public class PiecePersistenceConstants {
    /**
     * YAML 매핑 키 상수입니다.
     */
    @UtilityClass
    public class Keys {
        public final String ID = "id";
        public final String TYPE = "type";
        public final String TEAM = "team";
        public final String ROLE = "role";
    }
}
