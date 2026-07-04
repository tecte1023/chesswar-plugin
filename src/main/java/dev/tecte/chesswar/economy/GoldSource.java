package dev.tecte.chesswar.economy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GoldSource {
    STIPEND("봉급", 50),
    BOUNTY("포상금", 200),
    LEVY("징세", 100),
    PLUNDER("약탈금", 100);

    private final String description;
    private final int amount;
}
