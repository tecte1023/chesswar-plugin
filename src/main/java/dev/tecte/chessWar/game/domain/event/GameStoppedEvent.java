package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

/**
 * 게임 중단을 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GameStoppedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final List<UnitPiece> units;
    private final UUID stopperId;

    /**
     * 게임 중단 이벤트를 생성합니다.
     *
     * @param units     중단 당시 기물 목록
     * @param stopperId 중단 행위자 ID
     * @return 게임 중단 이벤트
     */
    @NonNull
    public static GameStoppedEvent of(@NonNull List<UnitPiece> units, @NonNull UUID stopperId) {
        return new GameStoppedEvent(List.copyOf(units), stopperId);
    }

    @NonNull
    @Override
    public UUID senderId() {
        return stopperId;
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
