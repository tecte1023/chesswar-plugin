package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * 기물 조사 요청을 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PieceInspectionRequestedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerId;
    private final UUID targetPieceId;

    /**
     * 기물 조사 요청 이벤트를 생성합니다.
     *
     * @param playerId      플레이어 ID
     * @param targetPieceId 대상 기물 ID
     * @return 기물 조사 요청 이벤트
     */
    @NonNull
    public static PieceInspectionRequestedEvent of(@NonNull UUID playerId, @NonNull UUID targetPieceId) {
        return new PieceInspectionRequestedEvent(playerId, targetPieceId);
    }

    @NonNull
    @Override
    public UUID senderId() {
        return playerId;
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
