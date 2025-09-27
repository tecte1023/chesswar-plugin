package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.model.TeamColor;
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
    private final TeamRepository teamRepository;

    /**
     * 플레이어를 지정된 색상의 팀에 참가시킵니다.
     *
     * @param playerId  팀에 참가할 플레이어의 UUID
     * @param teamColor 참가할 팀의 색상
     */
    public void joinTeam(@NonNull UUID playerId, @NonNull TeamColor teamColor) {
        teamRepository.addPlayer(playerId, teamColor);
    }
}
