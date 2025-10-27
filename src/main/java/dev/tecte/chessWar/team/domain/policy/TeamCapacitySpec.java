package dev.tecte.chessWar.team.domain.policy;

import lombok.NonNull;

/**
 * 팀 최대 인원 수에 대한 명세를 정의하는 레코드입니다.
 *
 * @param lowerBound   최대 인원 수의 하한값
 * @param upperBound   최대 인원 수의 상한값
 * @param defaultValue 최대 인원 수의 기본값
 */
public record TeamCapacitySpec(
        int lowerBound,
        int upperBound,
        int defaultValue
) {
    private static final int LOWER_BOUND = 1;
    private static final int UPPER_BOUND = 8;
    private static final int DEFAULT_VALUE = 8;

    public TeamCapacitySpec {
        if (lowerBound < LOWER_BOUND) {
            throw new IllegalArgumentException("Lower bound must be positive.");
        }

        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Upper bound must be greater than or equal to lower bound.");
        }

        if (defaultValue < lowerBound || defaultValue > upperBound) {
            throw new IllegalArgumentException("Default value must be within the bounds.");
        }
    }

    /**
     * 기본값으로 {@link TeamCapacitySpec} 인스턴스를 생성합니다.
     *
     * @return 기본 팀 최대 인원 수 명세
     */
    @NonNull
    public static TeamCapacitySpec defaultSpec() {
        return new TeamCapacitySpec(LOWER_BOUND, UPPER_BOUND, DEFAULT_VALUE);
    }
}
