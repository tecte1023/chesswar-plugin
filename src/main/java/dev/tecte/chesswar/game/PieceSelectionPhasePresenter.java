package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.PieceType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PieceSelectionPhasePresenter {
    public void showPieceDescription(
            @NotNull final Player player,
            @NotNull final PieceInspectionResult result
    ) {
        final PieceType type = result.type();
        final Component decorationLine = Component.text(
                "━━━━━━━━━━━━━━━",
                NamedTextColor.DARK_GRAY,
                TextDecoration.STRIKETHROUGH
        );
        final Component message = Component.text()
                .append(Component.newline())
                .append(decorationLine)
                .appendSpace()
                .append(Component.text(
                        "[ " + type.symbol() + " " + type.displayName() + " ]",
                        NamedTextColor.GOLD,
                        TextDecoration.BOLD
                ))
                .appendSpace()
                .append(decorationLine)
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text(type.description(), NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("체력: ", NamedTextColor.GRAY))
                .append(Component.text("♥ " + (int) type.baseHealth(), NamedTextColor.DARK_GREEN))
                .append(Component.text("  |  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("공격력: ", NamedTextColor.GRAY))
                .append(Component.text("⚔ " + (int) type.baseDamage(), NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.text("이동 및 공격 범위: ", NamedTextColor.GRAY))
                .append(Component.text(type.rangeDescription(), NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("               "))
                .append(buildJoinButton(result.selectability()))
                .append(Component.newline())
                .build();

        player.sendMessage(message);
        player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    @NotNull
    private Component buildJoinButton(@NotNull final PieceSelectability selectability) {
        final Component baseButton = Component.text()
                .content("[ ")
                .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
                .append(Component.text(" ]"))
                .build();

        return switch (selectability) {
            case SELECTABLE -> baseButton
                    .color(NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(
                            "클릭하여 해당 기물로 참전합니다.",
                            NamedTextColor.GREEN
                    )));
            case UNSELECTABLE_TEAM -> baseButton
                    .color(NamedTextColor.GRAY)
                    .hoverEvent(HoverEvent.showText(Component.text(
                            "자신의 팀 기물만 선택할 수 있습니다.",
                            NamedTextColor.RED
                    )));
            case UNSELECTABLE_TYPE -> baseButton
                    .color(NamedTextColor.GRAY)
                    .hoverEvent(HoverEvent.showText(Component.text(
                            "해당 기물은 선택할 수 없습니다.",
                            NamedTextColor.RED
                    )));
        };
    }
}
