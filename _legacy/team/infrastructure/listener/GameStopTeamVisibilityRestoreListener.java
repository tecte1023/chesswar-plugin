package dev.tecte.chessWar.team.infrastructure.listener;

import dev.tecte.chessWar.game.domain.event.GameStoppedEvent;
import dev.tecte.chessWar.team.application.TeamPhysicalApplier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 종료 후 플레이어 간 시야 제한 해제를 보장합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameStopTeamVisibilityRestoreListener implements Listener {
    private final TeamPhysicalApplier teamPhysicalApplier;

    /**
     * 참여자들이 서로를 정상적으로 식별할 수 있도록 가시성 설정을 초기화합니다.
     *
     * @param event 게임 중단 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onGameStopped(@NonNull GameStoppedEvent event) {
        teamPhysicalApplier.revealEnemies();
    }
}
