package dev.tecte.chessWar.game.infrastructure.persistence;

import lombok.experimental.UtilityClass;

/**
 * 게임 데이터의 영속성 처리와 관련된 상수들을 정의하는 클래스입니다.
 * YML 파일의 키 이름이나 경로 등을 관리합니다.
 */
@UtilityClass
public final class GamePersistenceConstants {
    public final String ROOT = "game";

    /**
     * YAML 파일이나 객체 매핑에 사용되는 순수 키 이름들을 정의합니다.
     */
    @UtilityClass
    public static class Keys {
        public final String STATE = "state";
        public final String PIECES = "pieces";
        public final String PHASE = "phase";
    }

    /**
     * 상태 파일의 전체 경로들을 정의합니다.
     */
    @UtilityClass
    public static class StatePaths {
        public final String STATE_PATH = ROOT + "." + Keys.STATE;
    }
}
