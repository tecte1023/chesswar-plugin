package dev.tecte.chessWar.team.domain.exception;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

public class TeamIsFullException extends RuntimeException {
    public TeamIsFullException(@NonNull TeamColor color) {
        super(String.format("Team %s is full.", color.name()));
    }
}
