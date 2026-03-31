package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.PieceSelectionCoordinator;
import dev.tecte.chessWar.game.domain.event.PieceInspectionRequestedEvent;
import dev.tecte.chessWar.port.EntityResolver;
import dev.tecte.chessWar.port.UserResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * 기물의 상세 정보 제공 프로세스를 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceInspectionInitiator implements Listener {
    private final PieceSelectionCoordinator pieceSelectionCoordinator;
    private final UserResolver userResolver;
    private final EntityResolver entityResolver;

    /**
     * 플레이어가 선택한 기물의 상세 정보를 제공합니다.
     *
     * @param event 조사 요청 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInspectionRequested(@NonNull PieceInspectionRequestedEvent event) {
        Optional<Player> foundPlayer = userResolver.findPlayer(event.playerId());

        if (foundPlayer.isEmpty()) {
            return;
        }

        Optional<Entity> foundPiece = entityResolver.findEntity(event.targetPieceId());

        if (foundPiece.isEmpty()) {
            return;
        }

        Player player = foundPlayer.get();
        Entity targetPiece = foundPiece.get();

        pieceSelectionCoordinator.inspectPiece(player, targetPiece);
    }
}
