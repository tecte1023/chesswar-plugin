package dev.tecte.chessWar.team.infrastructure.listener;

import dev.tecte.chessWar.game.domain.event.GameSelectionStartedEvent;
import dev.tecte.chessWar.team.application.TeamPhysicalApplier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 기물 선택 단계의 물리적 시작 지점 배치를 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameSelectionStartPlayerDeployListener implements Listener {
    private final TeamPhysicalApplier teamPhysicalApplier;

    /**
     * 참여자들을 각 진영의 시작 위치로 이동시켜 준비를 완료합니다.
     *
     * @param event 기물 선택 시작 이벤트
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onGameSelectionStarted(@NonNull GameSelectionStartedEvent event) {
        teamPhysicalApplier.deployAllTeams(event.game().board());
    }
}
