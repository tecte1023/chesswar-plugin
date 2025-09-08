package dev.tecte.chessWar.infrastructure.persistence;

/**
 * 영속성이 필요한 상태를 가진 객체가 구현해야 하는 인터페이스입니다.
 * 이 인터페이스를 구현하는 클래스는 플러그인 활성화 시 데이터를 로드하고,
 * 비활성화 시 데이터를 저장하는 생명주기에 참여하게 됩니다.
 */
public interface PersistableState {
    /**
     * 영속성 저장소에서 모든 데이터를 읽어와 메모리에 적재합니다.
     */
    void loadAll();

    /**
     * 메모리에 있는 모든 데이터를 영속성 저장소에 저장합니다.
     */
    void persistCache();
}
