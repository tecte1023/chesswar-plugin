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
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameNotifier {
    private final SenderNotifier senderNotifier;
    private final TeamService teamService;
    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    /**
     * 직업 선택 단계 시작을 알립니다. (타이틀 전송)
     *
     * @param participants 알림을 받을 참가자 목록
     */
    public void announceClassSelectionStart(@NonNull Set<Player> participants) {
        Component title = Component.text("직업을 선택해 주세요").decorate(TextDecoration.BOLD);

        for (Player player : participants) {
            senderNotifier.sendTitle(player, title);
        }
    }

    /**
     * 직업 선택 가이드를 시작합니다. (액션바 반복 전송)
     *
     * @return 실행 중인 가이드 태스크
     */
    @NonNull
    public BukkitTask startClassSelectionGuidance() {
        Component actionBar = Component.text("기물을 우클릭하여 직업을 선택해주세요.").color(NamedTextColor.RED);

        return scheduler.runTaskTimer(plugin, () -> {
            for (Player player : teamService.getAllOnlinePlayers()) {
                senderNotifier.sendActionBar(player, actionBar);
            }
        }, 0L, 40L);
    }
}
