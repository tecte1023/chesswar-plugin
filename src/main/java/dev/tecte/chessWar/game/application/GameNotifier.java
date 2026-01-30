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
    private static final Component PIECE_SELECTION_TITLE =
            Component.text("기물 선택").decorate(TextDecoration.BOLD).color(NamedTextColor.AQUA);
    private static final Component PIECE_SELECTION_GUIDE =
            Component.text("기물을 우클릭하여 참전할 기물을 선택해주세요.").color(NamedTextColor.RED);
    private static final Component GAME_STOP_MESSAGE = Component.text("게임이 중단되었습니다.");

    private static final long GUIDE_INITIAL_DELAY_TICKS = 0L;
    private static final long GUIDE_INTERVAL_TICKS = 40L;

    private final GameTaskScheduler gameTaskScheduler;
    private final TeamService teamService;
    private final SenderNotifier senderNotifier;

    /**
     * 기물 선택 단계의 시작을 알립니다.
     *
     * @param recipients 알림을 받을 대상
     */
    public void announcePieceSelectionStart(@NonNull Set<Player> recipients) {
        recipients.forEach(player -> senderNotifier.sendTitle(player, PIECE_SELECTION_TITLE));
    }

    /**
     * 기물 선택 가이드 안내를 시작합니다.
     */
    public void startPieceSelectionGuidance() {
        gameTaskScheduler.scheduleRepeat(
                GameTaskType.GUIDANCE,
                () -> broadcastActionBar(PIECE_SELECTION_GUIDE),
                GUIDE_INITIAL_DELAY_TICKS,
                GUIDE_INTERVAL_TICKS
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
        senderNotifier.notifySuccess(recipient, GAME_STOP_MESSAGE);
    }

    private void broadcastActionBar(Component message) {
        teamService.getAllOnlinePlayers().forEach(player -> senderNotifier.sendActionBar(player, message));
    }
}
