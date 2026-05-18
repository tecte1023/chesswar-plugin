package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.board.domain.model.Board;
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
 * 기물 소환 완료를 기록합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PiecesSpawnedEvent extends DomainEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Board board;
    private final Map<Coordinate, UnitPiece> unitPlacements;
    private final Map<UUID, TeamColor> participants;
    private final UUID starterId;

    /**
     * 기물 소환 완료 이벤트를 생성합니다.
     *
     * @param board          체스판
     * @param unitPlacements 기물 배치 현황
     * @param participants   참여자 정보
     * @param starterId      시작 행위자 ID
     * @return 기물 소환 완료 이벤트
     */
    @NonNull
    public static PiecesSpawnedEvent of(
            @NonNull Board board,
            @NonNull Map<Coordinate, UnitPiece> unitPlacements,
            @NonNull Map<UUID, TeamColor> participants,
            @NonNull UUID starterId
    ) {
        return new PiecesSpawnedEvent(board, Map.copyOf(unitPlacements), Map.copyOf(participants), starterId);
    }

    @NonNull
    @Override
    public UUID senderId() {
        return starterId;
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
