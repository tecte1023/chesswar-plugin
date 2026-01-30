package dev.tecte.chessWar.piece.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * 플레이어와 기물 간의 상호작용 이벤트를 처리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceInteractionListener implements Listener {
    private final GameService gameService;

    /**
     * 기물을 우클릭했을 때 상세 정보를 요청합니다.
     *
     * @param event 상호작용 이벤트
     */
    @EventHandler
    public void onRightClickAtPiece(@NonNull PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // 불필요한 서비스 호출 방지를 위해 플레이어 대상 클릭 무시
        if (event.getRightClicked() instanceof Player) {
            return;
        }

        gameService.inspectPiece(event.getPlayer(), event.getRightClicked());
    }
}
