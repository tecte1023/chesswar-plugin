package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.piece.InitialLayoutPolicy;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.TeamSide;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PieceSelectionPhasePresenter {
    @NotNull
    private final Component[][] descriptionCache;

    @NotNull
    public static PieceSelectionPhasePresenter create() {
        final var cache = new Component[TeamSide.values().length][Coordinate.SQUARE_COUNT];

        for (int flatIndex = 0; flatIndex < Coordinate.SQUARE_COUNT; flatIndex++) {
            final var coordinate = Coordinate.fromFlatIndex(flatIndex);
            final PieceType type = InitialLayoutPolicy.initialPieceType(coordinate);
            final TeamSide ownerTeam = InitialLayoutPolicy.teamSide(coordinate);

            if (type == null || ownerTeam == null) {
                continue;
            }

            for (final TeamSide viewerTeam : TeamSide.values()) {
                final var selectability = PieceSelectability.evaluate(type, ownerTeam, viewerTeam);

                cache[viewerTeam.ordinal()][flatIndex] = buildPieceDescription(type, selectability, coordinate);
            }
        }

        return new PieceSelectionPhasePresenter(cache);
    }

    @NotNull
    private static Component buildPieceDescription(
            @NotNull final PieceType type,
            @NotNull final PieceSelectability selectability,
            @NotNull final Coordinate coordinate
    ) {
        final Component decorationLine = Component.text(
                "━━━━━━━━━━━━━━━",
                NamedTextColor.DARK_GRAY,
                TextDecoration.STRIKETHROUGH
        );

        return Component.text()
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
                .append(buildJoinButton(selectability, coordinate))
                .append(Component.newline())
                .build();
    }

    @NotNull
    private static Component buildJoinButton(
            @NotNull final PieceSelectability selectability,
            @NotNull final Coordinate coordinate
    ) {
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
                    )))
                    .clickEvent(ClickEvent.runCommand("/cw select " + coordinate.x() + " " + coordinate.y()));
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

    public void showPieceDescription(
            @NotNull final Player player,
            @NotNull final Coordinate coordinate,
            @NotNull final TeamSide viewerTeam
    ) {
        final Component cachedComponent = descriptionCache[viewerTeam.ordinal()][coordinate.flatIndex()];

        if (cachedComponent == null) {
            return;
        }

        player.sendMessage(cachedComponent);
        player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    public void showPieceSelectedFeedback(@NotNull final Player player) {
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1.0f, 1.0f);
    }

    public void showPieceAlreadyTakenFeedback(@NotNull final Player player) {
        player.sendMessage(Component.text("이미 선택된 기물입니다.", NamedTextColor.RED));
        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }
}
