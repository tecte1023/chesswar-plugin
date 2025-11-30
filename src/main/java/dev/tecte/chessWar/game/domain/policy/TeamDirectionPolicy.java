package dev.tecte.chessWar.game.domain.policy;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.game.domain.model.PieceSpec;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.util.Vector;

/**
 * 도메인 서비스로서, 팀의 방향과 관련된 정책(규칙)을 결정합니다.
 */
@Singleton
public class TeamDirectionPolicy {
    /**
     * 특정 기물이 주어진 보드 상황에 따라 어느 방향을 바라봐야 하는지 계산합니다.
     *
     * @param spec  확인할 기물의 명세
     * @param board 현재 보드
     * @return 해당 기물이 바라봐야 할 방향 벡터
     */
    @NonNull
    public Vector calculateFacingVector(@NonNull PieceSpec spec, @NonNull Board board) {
        TeamColor teamColor = spec.teamColor();
        Orientation orientation = board.orientation();

        return teamColor == TeamColor.WHITE ? orientation.forward() : orientation.backward();
    }
}
