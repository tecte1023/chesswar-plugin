package dev.tecte.chessWar.game.application;

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

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameNotifier {
    private static final Component CLASS_SELECTION_TITLE =
            Component.text("직업 선택").decorate(TextDecoration.BOLD).color(NamedTextColor.AQUA);
    private static final Component CLASS_SELECTION_GUIDE =
            Component.text("기물을 우클릭하여 직업을 선택해주세요.").color(NamedTextColor.RED);
    private static final Component GAME_STOP_MESSAGE = Component.text("게임이 종료되었습니다.");

    private static final long GUIDE_INITIAL_DELAY_TICKS = 0L;
    private static final long GUIDE_INTERVAL_TICKS = 40L;

    private final GameTaskScheduler gameTaskScheduler;
    private final TeamService teamService;
    private final SenderNotifier senderNotifier;

    /**
     * 직업 선택 단계 시작을 알립니다. (타이틀 전송)
     *
     * @param participants 알림을 받을 참가자 목록
     */
    public void announceClassSelectionStart(@NonNull Set<Player> participants) {
        participants.forEach(player -> senderNotifier.sendTitle(player, CLASS_SELECTION_TITLE));
    }

    /**
     * 직업 선택 가이드를 시작합니다. (액션바 반복 전송)
     */
    public void startClassSelectionGuidance() {
        gameTaskScheduler.scheduleRepeat(
                GameTaskType.GUIDANCE,
                () -> broadcastActionBar(CLASS_SELECTION_GUIDE),
                GUIDE_INITIAL_DELAY_TICKS,
                GUIDE_INTERVAL_TICKS
        );
    }

    /**
     * 진행 중인 가이드 태스크를 중단하고 액션바를 즉시 지웁니다.
     */
    public void stopGuidance() {
        gameTaskScheduler.stop(GameTaskType.GUIDANCE);
        broadcastActionBar(Component.empty());
    }

    /**
     * 게임이 중단되었음을 알립니다.
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
