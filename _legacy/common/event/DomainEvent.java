package dev.tecte.chessWar.common.event;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;

import java.time.Instant;

/**
 * 도메인의 비즈니스 상태 변화를 기록합니다.
 */
@Getter
@Accessors(fluent = true)
public abstract class DomainEvent extends Event implements NotifiableEvent {
    private final Instant occurredAt;

    protected DomainEvent() {
        this.occurredAt = Instant.now();
    }
}
