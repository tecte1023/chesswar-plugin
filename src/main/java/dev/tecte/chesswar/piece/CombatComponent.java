package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.core.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CombatComponent implements Component {
    private int hp;
    private final int atk;

    public void applyDamage(final int amount) {
        hp = Math.max(0, hp - amount);
    }

    public boolean isDead() {
        return hp <= 0;
    }
}
