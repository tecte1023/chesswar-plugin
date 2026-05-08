package dev.tecte.chessWar.piece.domain.model;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

import java.util.UUID;

/**
 * 전장에서 전투를 수행하는 모든 기물을 나타냅니다.
 * <p>
 * 일반 기물이나 영웅 기물 모두 하나의 기물로 식별하고 관리합니다.
 */
public interface Piece {
    /**
     * 기물의 고유 식별자를 반환합니다.
     *
     * @return 기물의 식별자
     */
    @NonNull
    UUID id();

    /**
     * 기물의 정적 명세를 반환합니다.
     *
     * @return 기물의 정적 명세
     */
    @NonNull
    PieceSpec spec();

    /**
     * 기물의 게임 내 역할을 반환합니다.
     *
     * @return 기물의 역할
     */
    @NonNull
    PieceRole role();

    /**
     * 해당 팀에 소속된 기물인지 확인합니다.
     *
     * @param team 확인할 팀
     * @return 소속 여부
     */
    default boolean isTeam(@NonNull TeamColor team) {
        return spec().teamColor() == team;
    }

    /**
     * 해당 기물이 플레이어의 선택 대상이 될 수 있는지 확인합니다.
     *
     * @return 선택 가능 여부
     */
    default boolean isSelectable() {
        return false;
    }
}
