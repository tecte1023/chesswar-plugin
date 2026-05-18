package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.common.identity.ProjectIdentity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * 모든 참가자가 기물 선택을 완료했을 때 발생합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AllParticipantsSelectedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 모든 참가자의 기물 선택 완료 이벤트를 생성합니다.
     *
     * @return 기물 선택 완료 이벤트
     */
    @NonNull
    public static AllParticipantsSelectedEvent create() {
        return new AllParticipantsSelectedEvent();
    }

    @NonNull
    @Override
    public UUID senderId() {
        return ProjectIdentity.SYSTEM_ID;
    }

    @NonNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NonNull
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
