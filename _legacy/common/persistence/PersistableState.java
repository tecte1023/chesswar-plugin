package dev.tecte.chessWar.common.persistence;

/**
 * 영속성 관리가 필요한 상태입니다.
 */
public interface PersistableState {
    /**
     * 저장소에서 상태를 불러와 초기화합니다.
     */
    void load();

    /**
     * 캐시된 상태를 저장소에 반영합니다.
     */
    void flush();
}
