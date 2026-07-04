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
public final class ActionMaskComponent {
    private long moveMask;
    private long attackMask;
    private int lastVersion;
}
