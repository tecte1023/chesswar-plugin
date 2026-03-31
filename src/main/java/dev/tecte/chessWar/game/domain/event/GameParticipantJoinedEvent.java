package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
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
 * 참여자의 게임 복귀를 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GameParticipantJoinedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerId;
    private final TeamColor playerTeam;
    private final Map<Coordinate, UnitPiece> unitPlacements;

    /**
     * 참여자 복귀 이벤트를 생성합니다.
     *
     * @param playerId       플레이어 ID
     * @param playerTeam     참여 팀
     * @param unitPlacements 기물 배치 현황
     * @return 참여자 복귀 이벤트
     */
    @NonNull
    public static GameParticipantJoinedEvent of(
            @NonNull UUID playerId,
            @NonNull TeamColor playerTeam,
            @NonNull Map<Coordinate, UnitPiece> unitPlacements
    ) {
        return new GameParticipantJoinedEvent(playerId, playerTeam, Map.copyOf(unitPlacements));
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
