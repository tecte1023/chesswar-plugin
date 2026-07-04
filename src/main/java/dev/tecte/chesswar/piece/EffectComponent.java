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
public final class EffectComponent {
    private long effectMask;
    private final int[] expirations;
}
