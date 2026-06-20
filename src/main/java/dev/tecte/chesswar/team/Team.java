package dev.tecte.chesswar.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum Team {
    WHITE("백팀", NamedTextColor.WHITE),
    BLACK("흑팀", NamedTextColor.DARK_GRAY);

    private final String teamName;
    private final NamedTextColor color;
}
