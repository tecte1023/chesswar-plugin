package dev.tecte.chessWar.team.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

/**
 * 팀 도메인 규칙 위반 시 발생하는 비즈니스 예외입니다.
 * <p>
 * 이 예외는 로그를 남기지 않고, 사용자에게 알림 메시지만 전달합니다.
 */
public class TeamException extends BusinessException {
    private TeamException(@NonNull String message) {
        super(message);
    }

    /**
     * 팀 정원 초과 시 사용자 알림 예외를 생성합니다.
     *
     * @param teamColor 정원이 가득 찬 팀
     * @return {@link TeamException} 인스턴스
     */
    @NonNull
    public static TeamException capacityExceeded(@NonNull TeamColor teamColor) {
        return new TeamException(teamColor.displayName() + "은 이미 가득 찼습니다.");
    }
}
