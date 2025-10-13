package dev.tecte.chessWar.team.domain.exception;

import dev.tecte.chessWar.common.exception.PlayerNotifiableException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * 플레이어가 가득 찬 팀에 참가를 시도할 때 발생하는 예외입니다.
 */
@Getter
public class TeamFullException extends PlayerNotifiableException {
    /**
     * @param teamColor 가득 찬 팀의 색상
     * @param playerId  참가를 시도한 플레이어의 UUID
     */
    public TeamFullException(@NonNull TeamColor teamColor, @NonNull UUID playerId) {
        super(teamColor.getName() + "팀은 이미 가득 찼습니다.", playerId);
    }
}
