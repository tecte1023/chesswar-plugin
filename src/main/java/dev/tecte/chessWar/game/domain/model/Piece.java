package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.Builder;

import java.util.Objects;

/**
 * 체스 기물의 설계도를 나타내는 불변 객체입니다.
 *
 * @param type      기물의 종류
 * @param teamColor 팀 색상
 * @param mobId     해당 기물에 매핑되는 Mythic Mob의 ID
 */
@Builder
public record Piece(
        PieceType type,
        TeamColor teamColor,
        String mobId
) {
    public Piece {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(teamColor, "TeamColor cannot be null");
        Objects.requireNonNull(mobId, "MobId cannot be null");
    }
}
