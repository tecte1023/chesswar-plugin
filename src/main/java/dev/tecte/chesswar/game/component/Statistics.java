package dev.tecte.chesswar.game.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Statistics {
    private double damageDealt = 0;
    private double damageTaken = 0;
    private double healingDone = 0;
    private int goldEarned = 0;
    private int goldSpent = 0;
    private int kills = 0;
    private int deaths = 0;
    private int assists = 0;

    public void addDamageDealt(double damage) {
        this.damageDealt += damage;
    }

    public void addDamageTaken(double damage) {
        this.damageTaken += damage;
    }

    public void addHealingDone(double healing) {
        this.healingDone += healing;
    }

    public void addGoldEarned(int gold) {
        this.goldEarned += gold;
    }

    public void addGoldSpent(int gold) {
        this.goldSpent += gold;
    }

    public void addKill() {
        this.kills++;
    }

    public void addDeath() {
        this.deaths++;
    }

    public void addAssist() {
        this.assists++;
    }
}
