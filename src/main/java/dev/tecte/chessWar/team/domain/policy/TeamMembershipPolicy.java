package dev.tecte.chessWar.team.domain.policy;

import dev.tecte.chessWar.team.domain.exception.TeamFullException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

import java.util.UUID;

/**
 * 팀 멤버십(가입, 탈퇴, 추방 등)과 관련된 비즈니스 규칙을 정의하는 정책 인터페이스입니다.
 */
public interface TeamMembershipPolicy {
    /**
     * 플레이어가 특정 팀에 가입할 수 있는지 확인합니다.
     * 정책을 만족하지 못하면 예외를 발생시킵니다.
     *
     * @param teamColor 확인할 팀 색상
     * @param playerId  확인할 플레이어의 UUID
     * @throws TeamFullException 팀이 가득 차서 가입할 수 없는 경우
     */
    void checkIfJoinable(@NonNull TeamColor teamColor, @NonNull UUID playerId) throws TeamFullException;
}
