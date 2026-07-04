package dev.tecte.chesswar.economy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@AllArgsConstructor
public class GoldComponent {
    private int currentGold;
}
