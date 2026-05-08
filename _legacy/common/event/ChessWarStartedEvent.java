package dev.tecte.chessWar.common.event;

import dev.tecte.chessWar.common.identity.ProjectIdentity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * 체스워 서비스 시작 및 데이터 준비 완료를 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChessWarStartedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 체스워 시작 이벤트를 생성합니다.
     *
     * @return 체스워 시작 이벤트
     */
    @NonNull
    public static ChessWarStartedEvent of() {
        return new ChessWarStartedEvent();
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
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
