package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.game.domain.model.Game;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * 게임 시작을 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GameStartedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Game game;
    private final UUID senderId;

    /**
     * 게임 시작 이벤트를 생성합니다.
     *
     * @param game     시작된 게임
     * @param senderId 시작 행위자 ID
     * @return 게임 시작 이벤트
     */
    @NonNull
    public static GameStartedEvent of(@NonNull Game game, @NonNull UUID senderId) {
        return new GameStartedEvent(game, senderId);
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
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
