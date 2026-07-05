/*
package dev.tecte.chesswar.piece;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

*/
/**
 * [Value Object] 팀별 기물 스탯 강화 수치를 표현하는 불변 객체.
 *//*

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StatBuff {
    private final double health;
    private final double damage;

    @NotNull
    public static StatBuff of(final double health, final double damage) {
        return new StatBuff(health, damage);
    }

    @NotNull
    public static StatBuff empty() {
        return new StatBuff(0.0, 0.0);
    }

    @NotNull
    public StatBuff withHealth(final double amount) {
        return new StatBuff(health + amount, damage);
    }

    @NotNull
    public StatBuff withDamage(final double amount) {
        return new StatBuff(health, damage + amount);
    }
}
*/
