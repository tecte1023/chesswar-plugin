package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.exception.TeamIsFullException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamService {
    private final TeamRepository teamRepository;

    public void joinTeam(@NonNull UUID playerId, @NonNull TeamColor teamColor) {
        if (teamRepository.getSize(teamColor) >= teamRepository.getMaxPlayers(teamColor)) {
            throw new TeamIsFullException(teamColor);
        }

        teamRepository.addPlayer(playerId, teamColor);
    }
}
