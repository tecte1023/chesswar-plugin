package dev.tecte.chessWar.team.application.port;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

import java.util.UUID;

public interface TeamRepository {
    int getSize(@NonNull TeamColor teamColor);

    int getMaxPlayers(@NonNull TeamColor teamColor);

    void addPlayer(@NonNull UUID playerId, @NonNull TeamColor teamColor);
}
