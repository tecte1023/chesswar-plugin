package dev.tecte.chesswar.piece;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

/**
 * 기물에 적용되는 상태 효과를 나타내는 클래스.
 *
 * <p>이름({@link #name})과 종류({@link #type})는 효과의 정체성이므로 {@code final}로 선언하고,
 * 매 턴마다 감소하는 지속 시간({@link #durationTurns})은 가변 필드로 둔다.
 *
 * <pre>{@code
 * // 사용 예시
 * PieceEffect leap = PieceEffect.of("도약", EffectType.BUFF, 1);
 * piece.addEffect(leap);
 * }</pre>
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
public class PieceEffect {

    /** 효과 식별 이름 (예: "도약", "기절"). */
    private final String name;

    /** 효과 종류 – 버프 또는 디버프. */
    private final EffectType type;

    /** 남은 지속 턴 수. 0 이하가 되면 만료된 것으로 간주한다. */
    private int durationTurns;

    public PieceEffect(@NotNull final String name, @NotNull final EffectType type, final int durationTurns) {
        this.name = name;
        this.type = type;
        this.durationTurns = durationTurns;
    }

    /**
     * {@link PieceEffect}의 정적 팩토리 메서드.
     *
     * @param name          효과 이름
     * @param type          효과 종류
     * @param durationTurns 지속 턴 수
     * @return 새로운 {@link PieceEffect} 인스턴스
     */
    @NotNull
    public static PieceEffect of(@NotNull final String name, @NotNull final EffectType type, final int durationTurns) {
        return new PieceEffect(name, type, durationTurns);
    }

    /**
     * 효과가 만료되었는지 확인한다.
     *
     * @return {@code durationTurns}가 0 이하이면 {@code true}
     */
    public boolean isExpired() {
        return durationTurns <= 0;
    }

    /** 지속 턴 수를 1 감소시킨다. */
    public void decrementDuration() {
        this.durationTurns--;
    }
}
