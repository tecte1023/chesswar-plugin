package dev.tecte.chessWar.piece.application.port.dto;

/**
 * 기물의 기본 스탯 정보를 담는 DTO입니다.
 * <p>
 * Infrastructure 계층에서 데이터를 읽어와 Application 계층으로 전달하는 용도로 사용됩니다.
 *
 * @param maxHealth    최대 체력
 * @param attackDamage 공격력
 */
public record PieceStatsDto(double maxHealth, double attackDamage) {
    /**
     * 외부 데이터 소스로부터 정보를 불러오지 못했을 때 사용하는 기본값입니다.
     */
    // [설계 결정]
    // DDD 관점에서는 별도의 비즈니스 정책으로 분리할 수 있으나,
    // 현재 프로젝트 규모에서는 인프라 데이터 누락에 대한 Fallback 기준을 과도하게 분리하기보다
    // DTO 내부 상수로 관리하는 것이 유지보수와 가독성 측면에서 더 실용적이라고 판단함
    public static final PieceStatsDto DEFAULT = new PieceStatsDto(20.0, 1.0);
}
