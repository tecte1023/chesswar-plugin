package dev.tecte.chessWar.team.domain.policy;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

/**
 * 팀의 최대 인원 수를 결정하는 기본 정책 구현체입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultTeamCapacityPolicy implements TeamCapacityPolicy {
    private final TeamCapacitySpec spec;

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyTo(int value) {
        if (value < spec.lowerBound() || value > spec.upperBound()) {
            return spec.defaultValue();
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lowerBound() {
        return spec.lowerBound();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int upperBound() {
        return spec.upperBound();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int defaultValue() {
        return spec.defaultValue();
    }
}
