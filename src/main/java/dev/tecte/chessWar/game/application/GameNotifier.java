package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.game.application.port.GameTaskScheduler;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import dev.tecte.chessWar.team.application.TeamService;
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
 * 게임 알림 및 안내 메시지를 전송합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameNotifier {
    private final TeamService teamService;
    private final GameTaskScheduler gameTaskScheduler;
    private final SenderNotifier senderNotifier;

    /**
     * 기물 선택 단계의 시작을 알립니다.
     *
     * @param recipients 알림을 받을 대상
     */
    public void announcePieceSelectionStart(@NonNull Set<Player> recipients) {
        Component title = Component.text("기물 선택")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.AQUA);

        recipients.forEach(player -> senderNotifier.sendTitle(player, title));
    }

    /**
     * 기물 선택 가이드 안내를 시작합니다.
     */
    public void startPieceSelectionGuidance() {
        Component guideMessage = Component.text("기물을 우클릭하여 참전할 기물을 선택해주세요.")
                .color(NamedTextColor.RED);
        long initialDelay = 0L;
        long intervalTicks = 2 * 20L;

        gameTaskScheduler.scheduleRepeat(
                GameTaskType.GUIDANCE,
                () -> broadcastActionBar(guideMessage),
                initialDelay,
                intervalTicks
        );
    }

    /**
     * 진행 중인 가이드 안내를 중단합니다.
     */
    public void stopGuidance() {
        gameTaskScheduler.stop(GameTaskType.GUIDANCE);
        broadcastActionBar(Component.empty());
    }

    /**
     * 게임 중단을 알립니다.
     *
     * @param recipient 알림을 받을 대상
     */
    public void notifyGameStop(@NonNull CommandSender recipient) {
        senderNotifier.notifySuccess(recipient, "게임이 중단되었습니다.");
    }

    private void broadcastActionBar(Component message) {
        teamService.getAllOnlinePlayers().forEach(player -> senderNotifier.sendActionBar(player, message));
    }
}
