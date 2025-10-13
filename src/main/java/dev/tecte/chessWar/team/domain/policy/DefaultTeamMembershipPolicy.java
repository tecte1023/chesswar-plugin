package dev.tecte.chessWar.team.domain.policy;

import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.exception.TeamFullException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 팀 멤버십(가입, 탈퇴, 추방 등)과 관련된 비즈니스 규칙의 기본 구현체입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultTeamMembershipPolicy implements TeamMembershipPolicy {
    private final TeamRepository teamRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkIfJoinable(@NonNull TeamColor teamColor, @NonNull UUID playerId) {
        if (teamRepository.getSize(teamColor) >= teamRepository.getMaxPlayers()) {
            throw new TeamFullException(teamColor, playerId);
        }
    }
}
