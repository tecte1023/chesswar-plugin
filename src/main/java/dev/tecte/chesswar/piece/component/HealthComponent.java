package dev.tecte.chesswar.piece.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HealthComponent implements PieceComponent {
    private double currentHealth;
    private double maxHealth;

    public void damage(double amount) {
        currentHealth = Math.max(0, currentHealth - amount);
    }

    public void heal(double amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }
}
