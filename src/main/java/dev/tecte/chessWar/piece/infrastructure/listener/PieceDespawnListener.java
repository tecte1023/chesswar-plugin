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
 * 게임 종료 시점에 전장의 기물들을 제거합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceDespawnListener implements Listener {
    private final PieceService pieceService;

    /**
     * 전장에 배치된 기물들을 소환 해제합니다.
     *
     * @param event 게임 종료 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void despawnPieces(@NonNull GameStoppedEvent event) {
        pieceService.despawnPieces(event.unitPieces());
    }
}
