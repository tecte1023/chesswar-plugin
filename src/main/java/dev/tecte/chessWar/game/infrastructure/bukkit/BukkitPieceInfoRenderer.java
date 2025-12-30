package dev.tecte.chessWar.game.infrastructure.bukkit;

import dev.tecte.chessWar.game.application.port.PieceInfoRenderer;
import dev.tecte.chessWar.game.domain.exception.PieceEntityNotFoundException;
import dev.tecte.chessWar.game.domain.model.UnitPiece;
import dev.tecte.chessWar.game.domain.model.PieceType;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * {@link PieceInfoRenderer}의 Bukkit 구현체입니다.
 * <p>
 * MythicMobs 플러그인의 API를 사용하여 활성 몹의 실시간 정보를 조회하고,
 * 플레이어의 채팅창에 기물의 상세 정보를 렌더링합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitPieceInfoRenderer implements PieceInfoRenderer {
    private final MobExecutor mobExecutor;

    @Override
    public void renderInfo(@NonNull Player player, @NonNull UnitPiece piece) {
        ActiveMob activeMob = mobExecutor.getActiveMob(piece.entityId())
                .orElseThrow(PieceEntityNotFoundException::entityNotFound);
        Component title = buildTitle(piece);
        Component description = Component.text(piece.spec().type().getDescription(), NamedTextColor.WHITE);
        Component stats = buildStats(activeMob);
        Component range = buildRangeInfo(piece);
        Component buttons = buildPromotionButton(piece);

        player.sendMessage(Component.empty());
        player.sendMessage(title);
        player.sendMessage(Component.empty());
        player.sendMessage(description);
        player.sendMessage(Component.empty());
        player.sendMessage(stats);
        player.sendMessage(range);
        player.sendMessage(Component.empty());
        player.sendMessage(buttons);
        player.sendMessage(Component.empty());
    }

    @NonNull
    private Component buildTitle(@NonNull UnitPiece piece) {
        PieceType type = piece.spec().type();
        Component deco = Component.text("━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.STRIKETHROUGH);

        return Component.text()
                .append(deco)
                .append(Component.text(" [ ", NamedTextColor.DARK_GRAY))
                .append(Component.text(type.getSymbol() + " ", NamedTextColor.GOLD))
                .append(Component.text(type.getDisplayName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" ] ", NamedTextColor.DARK_GRAY))
                .append(deco)
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
                .append(Component.text(piece.spec().type().getRangeDescription(), NamedTextColor.AQUA))
                .build();
    }

    @NonNull
    private Component buildPromotionButton(@NonNull UnitPiece piece) {
        Component button;

        if (piece.isSelected()) {
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
                    .clickEvent(ClickEvent.runCommand("/chesswar piece select " + piece.entityId()))
                    .build();
        }

        return Component.text()
                .append(Component.text("               "))
                .append(button)
                .build();
    }
}
