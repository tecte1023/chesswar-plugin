package dev.tecte.chesswar.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum Team {
    WHITE("백팀", NamedTextColor.WHITE, 1),
    BLACK("흑팀", NamedTextColor.DARK_GRAY, -1);

    @NotNull
    private final String teamName;

    @NotNull
    private final NamedTextColor color;

    private final int direction;
}
