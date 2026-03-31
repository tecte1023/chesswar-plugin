package dev.tecte.chessWar.team.domain.policy;

/**
 * 팀 인원 제한 및 보정 기준을 관리합니다.
 *
 * @param minCapacity     최소 인원
 * @param maxCapacity     최대 인원
 * @param defaultCapacity 기본 인원
 */
public record TeamCapacityPolicy(int minCapacity, int maxCapacity, int defaultCapacity) {
    /**
     * 기본 인원으로 제한 정책을 생성합니다.
     *
     * @return 기본 정책
     */
    public static TeamCapacityPolicy defaultPolicy() {
        return new TeamCapacityPolicy(1, 8, 8);
    }

    /**
     * 인원이 가득 찼는지 확인합니다.
     *
     * @param playerCount 현재 인원
     * @return 가득 찼는지 여부
     */
    public boolean isFull(int playerCount) {
        return playerCount >= defaultCapacity;
    }

    /**
     * 인원이 부족한지 확인합니다.
     *
     * @param playerCount 현재 인원
     * @return 부족한지 여부
     */
    public boolean isLacking(int playerCount) {
        return playerCount < minCapacity;
    }

    /**
     * 입력값을 정책 범위 내로 조정합니다.
     *
     * @param value 조정할 값
     * @return 조정된 값
     */
    public int apply(int value) {
        return Math.max(minCapacity, Math.min(maxCapacity, value));
    }
}
