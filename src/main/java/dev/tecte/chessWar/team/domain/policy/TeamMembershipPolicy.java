package dev.tecte.chessWar.team.domain.policy;

import dev.tecte.chessWar.team.domain.exception.TeamException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

/**
 * 팀 멤버십과 관련된 도메인 정책을 정의합니다.
 */
public interface TeamMembershipPolicy {
    /**
     * 팀 가입 가능 여부를 검증합니다.
     *
     * @param teamColor 대상 팀
     * @throws TeamException 가입 조건을 충족하지 못한 경우
     */
    void checkIfJoinable(@NonNull TeamColor teamColor);
}
