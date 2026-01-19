package dev.tecte.chessWar.team.domain.model;

import lombok.NonNull;

/**
 * 팀 최대 인원 수에 대한 정책(규칙)을 정의하는 도메인 모델입니다.
 *
 * @param lowerBound   최대 인원 수의 하한값
 * @param upperBound   최대 인원 수의 상한값
 * @param defaultValue 최대 인원 수의 기본값
 */
public record TeamCapacityPolicy(
        int lowerBound,
        int upperBound,
        int defaultValue
) {
    private static final int LOWER_BOUND = 1;
    private static final int UPPER_BOUND = 8;
    private static final int DEFAULT_VALUE = 8;

    public TeamCapacityPolicy {
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
     * 기본값으로 {@link TeamCapacityPolicy} 인스턴스를 생성합니다.
     *
     * @return 기본 팀 최대 인원 수 정책
     */
    @NonNull
    public static TeamCapacityPolicy defaultPolicy() {
        return new TeamCapacityPolicy(LOWER_BOUND, UPPER_BOUND, DEFAULT_VALUE);
    }

    /**
     * 주어진 값이 정책이 허용하는 범위 내에 있는지 확인하고 조정합니다.
     * <p>
     * 값이 유효 범위를 벗어난 경우 {@link #defaultValue()}가 반환됩니다.
     *
     * @param value 검증 및 조정할 인원 수 값
     * @return 유효 범위 내의 값 또는 기본값
     */
    public int adjust(int value) {
        if (value < lowerBound || value > upperBound) {
            return defaultValue;
        }

        return value;
    }
}
