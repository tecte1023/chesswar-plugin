package dev.tecte.chesswar.economy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * [순수 데이터 객체] 재화 상태를 저장하는 컴포넌트.
 */
@Data
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class GoldComponent {
    private int currentGold;
    private int baseIncome;

    public static GoldComponent createDefault() {
        return GoldComponent.of(0, 50); // 기본 월급 50골드
    }

    public void add(int amount) {
        this.currentGold += amount;
    }

    public boolean subtract(int amount) {
        if (this.currentGold < amount) {
            return false;
        }
        this.currentGold -= amount;
        return true;
    }

    public boolean has(int amount) {
        return this.currentGold >= amount;
    }
}
