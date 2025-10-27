package dev.tecte.chessWar.team.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Notifiable;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.Getter;
import lombok.NonNull;

/**
 * 플레이어가 가득 찬 팀에 참가를 시도할 때 발생하는 예외입니다.
 */
@Getter
public class TeamFullException extends BusinessException implements Notifiable {
    public TeamFullException(@NonNull TeamColor teamColor) {
        super(teamColor.getName() + "은 이미 가득 찼습니다.");
    }
}
