package dev.tecte.chessWar.piece.infrastructure.bukkit;

import dev.tecte.chessWar.piece.application.port.PieceInfoRenderer;
import dev.tecte.chessWar.piece.domain.exception.PieceSystemException;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.piece.infrastructure.command.PieceCommandConstants;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * MythicMobs API를 사용하여 기물 정보를 채팅창에 표시합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitPieceInfoRenderer implements PieceInfoRenderer {
    private static final Component DECORATION_LINE = Component.text(
            "━━━━━━━━━━━━━━━",
            NamedTextColor.DARK_GRAY,
            TextDecoration.STRIKETHROUGH
    );

    private final MobExecutor mobExecutor;

    @Override
    public void renderInfo(@NonNull Player player, @NonNull UnitPiece piece, boolean isSelected) {
        ActiveMob activeMob = mobExecutor.getActiveMob(piece.id())
                .orElseThrow(() -> PieceSystemException.entityMissing(piece.id()));
        Component infoPanel = Component.join(
                JoinConfiguration.newlines(),
                Component.empty(),
                buildTitle(piece),
                Component.empty(),
                Component.text(piece.spec().type().description(), NamedTextColor.WHITE),
                Component.empty(),
                buildStats(activeMob),
                buildRangeInfo(piece),
                Component.empty(),
                buildPromotionButton(piece, isSelected),
                Component.empty()
        );

        player.sendMessage(infoPanel);
    }

    @NonNull
    private Component buildTitle(@NonNull UnitPiece piece) {
        return Component.text()
                .append(DECORATION_LINE)
                .appendSpace()
                .append(piece.spec().type().formattedName().color(NamedTextColor.GOLD))
                .appendSpace()
                .append(DECORATION_LINE)
                .build();
    }

    @NonNull
    private Component buildStats(@NonNull ActiveMob activeMob) {
        return Component.text()
                .append(Component.text("체력: ", NamedTextColor.GRAY))
                .append(Component.text("♥ " + (int) activeMob.getEntity().getHealth(), NamedTextColor.DARK_GREEN))
                .append(Component.text("  |  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("공격력: ", NamedTextColor.GRAY))
                .append(Component.text("⚔ " + (int) activeMob.getDamage(), NamedTextColor.RED))
                .build();
    }

    @NonNull
    private Component buildRangeInfo(@NonNull UnitPiece piece) {
        return Component.text()
                .append(Component.text("공격 및 이동 범위: ", NamedTextColor.GRAY))
                .append(Component.text(piece.spec().type().rangeDescription(), NamedTextColor.AQUA))
                .build();
    }

    @NonNull
    private Component buildPromotionButton(@NonNull UnitPiece piece, boolean isSelected) {
        Component button;

        if (isSelected) {
            button = Component.text()
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("[ ⚔ "))
                    .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
                    .append(Component.text(" ]"))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("이미 참전 중인 기물입니다.", NamedTextColor.RED)
                    ))
                    .build();
        } else {
            button = Component.text()
                    .color(NamedTextColor.GREEN)
                    .append(Component.text("[ ⚔ "))
                    .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
                    .append(Component.text(" ]"))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("클릭하여 해당 기물로 참전합니다.", NamedTextColor.GREEN)
                    ))
                    .clickEvent(ClickEvent.runCommand(PieceCommandConstants.buildSelectCommand(piece.id())))
                    .build();
        }

        return Component.text()
                .append(Component.text("               "))
                .append(button)
                .build();
    }
}
