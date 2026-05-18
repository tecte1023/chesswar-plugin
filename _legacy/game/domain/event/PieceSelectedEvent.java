package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * 기물 선택을 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PieceSelectedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerId;
    private final UUID pieceId;
    private final PieceSpec pieceSpec;

    /**
     * 기물 선택 이벤트를 생성합니다.
     *
     * @param playerId  플레이어 ID
     * @param pieceId   기물 ID
     * @param pieceSpec 기물 명세
     * @return 기물 선택 이벤트
     */
    @NonNull
    public static PieceSelectedEvent of(
            @NonNull UUID playerId,
            @NonNull UUID pieceId,
            @NonNull PieceSpec pieceSpec
    ) {
        return new PieceSelectedEvent(playerId, pieceId, pieceSpec);
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
