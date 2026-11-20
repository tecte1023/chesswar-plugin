package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.core.Component;
import lombok.Getter;

@Getter
public final class CombatComponent implements Component {
    private final int maxHp;
    private int hp;
    private final int atk;

    CombatComponent(final int maxHp, final int atk) {
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.atk = atk;
    }

    void applyDamage(final int amount) {
        hp = Math.max(0, hp - amount);
    }

    public boolean isDead() {
        return hp <= 0;
    }
}
