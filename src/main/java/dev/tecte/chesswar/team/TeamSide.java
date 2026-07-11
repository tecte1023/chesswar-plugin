package dev.tecte.chesswar.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum TeamSide {
    WHITE("백팀", NamedTextColor.WHITE, Material.WHITE_WOOL, 1),
    BLACK("흑팀", NamedTextColor.DARK_GRAY, Material.BLACK_WOOL, -1);

    @NotNull
    private final String teamName;

    @NotNull
    private final NamedTextColor color;

    @NotNull
    private final Material teamItem;

    private final int direction;

    @Nullable
    public static TeamSide fromMaterial(@NotNull final Material material) {
        for (final TeamSide teamSide : values()) {
            if (teamSide.teamItem == material) {
                return teamSide;
            }
        }
        
        return null;
    }
}
