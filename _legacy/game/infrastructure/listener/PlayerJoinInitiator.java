package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameFlowCoordinator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 플레이어 합류 프로세스의 진입점을 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerJoinInitiator implements Listener {
    private final GameFlowCoordinator gameFlowCoordinator;

    /**
     * 접속한 플레이어를 게임 세션 및 팀 시스템에 등록합니다.
     *
     * @param event 플레이어 접속 이벤트
     */
    @EventHandler
    public void onPlayerJoin(@NonNull PlayerJoinEvent event) {
        gameFlowCoordinator.handlePlayerJoin(event.getPlayer());
    }
}
