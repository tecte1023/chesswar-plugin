package dev.tecte.chessWar.team.infrastructure.listener;

import dev.tecte.chessWar.game.domain.event.GameStoppedEvent;
import dev.tecte.chessWar.team.application.TeamService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 종료 시점에 팀의 가시성 설정을 초기화합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamVisibilityResetListener implements Listener {
    private final TeamService teamService;

    /**
     * 모든 플레이어의 시야 제약을 해제합니다.
     *
     * @param event 게임 종료 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void revealEnemies(@NonNull GameStoppedEvent event) {
        teamService.revealEnemies();
    }
}
