package dev.tecte.chessWar.team.domain.policy;

import co.aikar.commands.ConditionFailedException;
import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 팀 멤버십(가입, 탈퇴, 추방 등)과 관련된 비즈니스 규칙의 기본 구현체입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultTeamMembershipPolicy implements TeamMembershipPolicy {
    private final TeamRepository teamRepository;

    @Override
    public void checkIfJoinable(@NonNull TeamColor teamColor) throws ConditionFailedException {
        if (teamRepository.getSize(teamColor) >= teamRepository.getMaxPlayers()) {
            throw new ConditionFailedException(teamColor.getName() + "팀은 이미 가득 찼습니다.");
        }
    }
}
