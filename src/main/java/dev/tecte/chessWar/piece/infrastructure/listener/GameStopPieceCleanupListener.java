package dev.tecte.chessWar.piece.infrastructure.listener;

import dev.tecte.chessWar.game.domain.event.GameStoppedEvent;
import dev.tecte.chessWar.piece.application.PieceService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 게임 종료 후 전장의 물리적 정리를 책임집니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameStopPieceCleanupListener implements Listener {
    private final PieceService pieceService;

    /**
     * 배치된 기물 엔티티를 월드에서 제거하여 초기 상태로 복구합니다.
     *
     * @param event 게임 종료 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onGameStopped(@NonNull GameStoppedEvent event) {
        pieceService.despawnPieces(event.units());
    }
}
