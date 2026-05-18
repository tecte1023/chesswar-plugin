package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;

import java.util.Map;
import java.util.UUID;

/**
 * 기물 선택 단계 시작을 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GameSelectionStartedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Game game;
    private final Map<UUID, TeamColor> participants;
    private final UUID senderId;

    /**
     * 기물 선택 시작 이벤트를 생성합니다.
     *
     * @param game         게임 상태
     * @param participants 참여자 정보 스냅샷
     * @param senderId     행위자 ID
     * @return 기물 선택 시작 이벤트
     */
    @NonNull
    public static GameSelectionStartedEvent of(
            @NonNull Game game,
            @NonNull Map<UUID, TeamColor> participants,
            @NonNull UUID senderId
    ) {
        return new GameSelectionStartedEvent(game, participants, senderId);
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
