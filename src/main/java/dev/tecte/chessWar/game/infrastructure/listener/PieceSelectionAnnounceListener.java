package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameAnnouncer;
import dev.tecte.chessWar.game.domain.event.PieceSelectedEvent;
import dev.tecte.chessWar.port.UserResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * 기물 선택 결과의 개인별 피드백을 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceSelectionAnnounceListener implements Listener {
    private final GameAnnouncer gameAnnouncer;
    private final UserResolver userResolver;

    /**
     * 참전할 기물이 확정되었음을 해당 플레이어에게 알립니다.
     *
     * @param event 기물 선택 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPieceSelected(@NonNull PieceSelectedEvent event) {
        Optional<Player> foundPlayer = userResolver.findPlayer(event.playerId());

        if (foundPlayer.isEmpty()) {
            return;
        }

        Player player = foundPlayer.get();
        
        gameAnnouncer.notifyPieceSelection(player, event.pieceSpec().type());
    }
}
