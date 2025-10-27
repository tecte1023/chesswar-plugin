package dev.tecte.chessWar.team.domain.policy;

/**
 * 팀의 최대 인원 수를 결정하는 정책 인터페이스입니다.
 * 이 정책은 팀당 최대 플레이어 수의 유효 범위를 정의하고, 주어진 값을 이 범위에 맞게 조정하는 역할을 합니다.
 */
public interface TeamCapacityPolicy {
    /**
     * 주어진 값을 정책에 따라 검증하고, 유효한 값으로 조정하여 반환합니다.
     *
     * @param value 조정할 최대 인원 수 값
     * @return 정책이 적용된 최대 인원 수 값
     */
    int applyTo(int value);

    /**
     * 정책의 하한값을 반환합니다.
     *
     * @return 하한값
     */
    int lowerBound();

    /**
     * 정책의 상한값을 반환합니다.
     *
     * @return 상한값
     */
    int upperBound();

    /**
     * 정책의 기본값을 반환합니다.
     *
     * @return 기본값
     */
    int defaultValue();
}
