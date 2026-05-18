package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameFlowCoordinator;
import dev.tecte.chessWar.game.domain.event.GameStartedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 시작에 따른 물리적 전장 조성을 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameStartBattlefieldSetupInitiator implements Listener {
    private final GameFlowCoordinator gameFlowCoordinator;

    /**
     * 체스판 구조를 바탕으로 기물을 소환합니다.
     *
     * @param event 게임 시작 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onGameStarted(@NonNull GameStartedEvent event) {
        gameFlowCoordinator.prepareBattlefield(event.game().board(), event.senderId());
    }
}
