package dev.tecte.chessWar.team.domain.policy;

import jakarta.inject.Singleton;

/**
 * 팀의 최대 인원 수를 결정하는 기본 정책 구현체입니다.
 */
@Singleton
public class DefaultTeamCapacityPolicy implements TeamCapacityPolicy {
    @Override
    public int applyTo(int value) {
        if (value < MAX_PLAYERS_LOWER_BOUND || value > MAX_PLAYERS_UPPER_BOUND) {
            return MAX_PLAYERS_DEFAULT;
        }

        return value;
    }
}
