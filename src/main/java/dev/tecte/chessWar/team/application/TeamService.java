package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.common.annotation.HandleExceptions;
import dev.tecte.chessWar.common.notifier.PlayerNotifier;
import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import dev.tecte.chessWar.team.domain.policy.TeamMembershipPolicy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 팀 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 플레이어의 팀 참가, 탈퇴 등과 같은 유스케이스를 담당합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamService {
    private static final String TEAM_JOIN_SUCCESS_SUFFIX = "팀에 참가했습니다.";

    private final TeamMembershipPolicy teamMembershipPolicy;
    private final TeamRepository teamRepository;
    private final PlayerNotifier playerNotifier;

    /**
     * 플레이어를 지정된 색상의 팀에 참가시킵니다.
     *
     * @param playerId  팀에 참가할 플레이어의 UUID
     * @param teamColor 참가할 팀의 색상
     */
    @HandleExceptions
    public void joinTeam(@NonNull UUID playerId, @NonNull TeamColor teamColor) {
        teamMembershipPolicy.checkIfJoinable(teamColor, playerId);
        teamRepository.addPlayer(playerId, teamColor);
        playerNotifier.notifySuccess(playerId, teamColor.getName() + TEAM_JOIN_SUCCESS_SUFFIX);
    }
}
