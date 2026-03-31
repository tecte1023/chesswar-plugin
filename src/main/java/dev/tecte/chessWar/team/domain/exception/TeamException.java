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
     * 팀 정원이 초과되었을 때 발생합니다.
     *
     * @param teamColor 대상 팀
     * @return 생성된 예외
     */
    @NonNull
    public static TeamException capacityExceeded(@NonNull TeamColor teamColor) {
        return new TeamException(teamColor.displayName() + "은 이미 가득 찼습니다.");
    }

    /**
     * 최소 인원 요건을 충족하지 못했을 때 발생합니다.
     *
     * @param minPlayers 최소 인원
     * @return 생성된 예외
     */
    @NonNull
    public static TeamException minimumCapacityNotMet(int minPlayers) {
        return new TeamException("각 팀에 최소 %d명의 플레이어가 필요합니다.".formatted(minPlayers));
    }

    /**
     * 소속된 팀이 없을 때 발생합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static TeamException notInTeam() {
        return new TeamException("참가 중인 팀이 없습니다.");
    }
}
