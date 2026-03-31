package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.port.UserNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * 게임 진행 중 발생하는 사건을 알립니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameAnnouncer {
    private final GameTaskManager gameTaskManager;
    private final UserNotifier userNotifier;

    /**
     * 기물 선택 시작을 표시합니다.
     *
     * @param targets 대상 플레이어
     */
    public void announceSelectionStart(@NonNull Set<Player> targets) {
        Component title = Component.text("기물 선택")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.AQUA);

        targets.forEach(player -> userNotifier.displayTitle(player, title));
    }

    /**
     * 기물 선택 가이드 메시지를 표시합니다.
     *
     * @param targets 대상 플레이어
     */
    public void startSelectionGuidance(@NonNull Set<Player> targets) {
        Component guideMessage = Component.text("기물을 우클릭하여 참전할 기물을 선택해주세요.")
                .color(NamedTextColor.RED);
        long initialDelay = 0L;
        long intervalTicks = 2 * 20L;

        gameTaskManager.runRepeating(
                GameTaskType.GUIDANCE,
                () -> targets.forEach(player -> userNotifier.displayActionBar(player, guideMessage)),
                initialDelay,
                intervalTicks
        );
    }

    /**
     * 기물 선택 완료를 알립니다.
     *
     * @param player    수신자
     * @param pieceType 선택된 기물 타입
     */
    public void notifyPieceSelection(@NonNull Player player, @NonNull PieceType pieceType) {
        userNotifier.informSuccess(
                player,
                Component.text()
                        .append(pieceType.formattedName().color(NamedTextColor.GOLD))
                        .append(Component.text(" 기물로 전장에 참전합니다."))
                        .build()
        );
    }

    /**
     * 게임 중단 성공을 알립니다.
     *
     * @param requester 행위자
     */
    public void notifyGameStop(@NonNull CommandSender requester) {
        userNotifier.informSuccess(requester, Component.text("게임이 중단되었습니다."));
    }

    /**
     * 가이드 메시지 표시를 중단합니다.
     */
    public void stopGuidance() {
        gameTaskManager.cancel(GameTaskType.GUIDANCE);
    }
}
