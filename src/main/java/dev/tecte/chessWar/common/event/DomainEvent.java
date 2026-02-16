package dev.tecte.chessWar.common.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.time.Instant;

/**
 * 도메인의 비즈니스 상태 변화를 기록합니다.
 */
@Getter
@Accessors(fluent = true)
public abstract class DomainEvent extends Event implements NotifiableEvent {
    private final Instant occurredAt;

    private static final HandlerList HANDLERS = new HandlerList();

    protected DomainEvent() {
        this.occurredAt = Instant.now();
    }

    /**
     * Bukkit 이벤트 시스템을 위한 핸들러 목록을 반환합니다.
     *
     * @return 핸들러 목록
     */
    @NonNull
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * 이벤트 인스턴스 호출에 필요한 핸들러 목록을 반환합니다.
     *
     * @return 핸들러 목록
     */
    @NonNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
