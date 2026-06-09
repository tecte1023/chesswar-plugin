package dev.tecte.chesswar.piece;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * [순수 데이터 객체] 팀별 기물 스탯 강화 정보를 저장합니다.
 */
@Data
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class StatBuff {
    private double health = 0.0;
    private double damage = 0.0;

    public static StatBuff create() {
        return new StatBuff();
    }

    public void addHealth(double amount) {
        this.health += amount;
    }

    public void addDamage(double amount) {
        this.damage += amount;
    }
}
