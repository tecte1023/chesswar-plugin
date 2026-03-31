package dev.tecte.chessWar.game.domain.policy;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.util.Vector;

/**
 * 팀별 진영 방향을 결정하는 정책을 관리합니다.
 */
@Singleton
public class TeamFacingPolicy {
    /**
     * 특정 팀의 전진 방향을 산출합니다.
     *
     * @param team  대상 팀
     * @param board 대상 체스판
     * @return 전진 방향
     */
    @NonNull
    public Vector forwardFacingOf(@NonNull TeamColor team, @NonNull Board board) {
        return (team == TeamColor.WHITE) ? board.forwardFacing() : board.backwardFacing();
    }

    /**
     * 아군 진영을 바라보는 방향을 산출합니다.
     *
     * @param team  대상 팀
     * @param board 대상 체스판
     * @return 아군 진영 방향
     */
    @NonNull
    public Vector ownCampFacingOf(@NonNull TeamColor team, @NonNull Board board) {
        return forwardFacingOf(team, board).multiply(-1);
    }
}
