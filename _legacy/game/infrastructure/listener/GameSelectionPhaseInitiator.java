package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameFlowCoordinator;
import dev.tecte.chessWar.game.domain.event.PiecesSpawnedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 전장 준비와 선택 단계 간의 상태 전이를 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameSelectionPhaseInitiator implements Listener {
    private final GameFlowCoordinator gameFlowCoordinator;

    /**
     * 배치된 기물 정보를 확정하고 기물을 선택 가능한 상태로 전환합니다.
     *
     * @param event 기물 소환 완료 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPiecesSpawned(@NonNull PiecesSpawnedEvent event) {
        gameFlowCoordinator.startSelectionPhase(
                event.unitPlacements(),
                event.participants(),
                event.senderId()
        );
    }
}
