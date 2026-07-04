package dev.tecte.chesswar.piece;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor
public final class AbilityComponent {
    private long abilityMask;
}
