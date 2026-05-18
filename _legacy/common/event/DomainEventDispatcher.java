package dev.tecte.chessWar.common.event;

import lombok.NonNull;

/**
 * 도메인 이벤트를 외부로 전파합니다.
 */
public interface DomainEventDispatcher {
    /**
     * 발생한 도메인 이벤트를 전파합니다.
     *
     * @param event 전파할 이벤트
     */
    void dispatch(@NonNull DomainEvent event);
}
