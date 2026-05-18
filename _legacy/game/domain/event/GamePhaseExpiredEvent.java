package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * 특정 게임 단계의 제한 시간이 만료되었음을 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GamePhaseExpiredEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final GamePhase expiredPhase;
    private final UUID senderId;

    /**
     * 단계 만료 이벤트를 생성합니다.
     *
     * @param expiredPhase 만료된 단계
     * @param senderId     행위자 ID
     * @return 단계 만료 이벤트
     */
    @NonNull
    public static GamePhaseExpiredEvent of(@NonNull GamePhase expiredPhase, @NonNull UUID senderId) {
        return new GamePhaseExpiredEvent(expiredPhase, senderId);
    }

    @NonNull
    @Override
    public UUID senderId() {
        return senderId;
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
