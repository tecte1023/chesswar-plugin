package dev.tecte.chesswar.piece;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@AllArgsConstructor
public final class StatComponent {
    private double currentHealth;
    private double maxHealth;
    private double attackDamage;
}
