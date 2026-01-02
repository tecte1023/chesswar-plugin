package dev.tecte.chessWar.piece.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * 플레이어와 기물 간의 상호작용 이벤트를 처리하는 리스너입니다.
 * <p>
 * Bukkit 이벤트를 감지하여 적절한 도메인 서비스 로직으로 연결하는 어댑터 역할을 수행합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceInteractionListener implements Listener {
    private final GameService gameService;

    /**
     * 플레이어가 엔티티(기물) 우클릭했을 때 호출되는 이벤트 핸들러입니다.
     * <p>
     * 기물 검사 및 정보 표시 로직을 수행하기 위해 도메인 서비스로 처리를 위임합니다.
     *
     * @param event Bukkit 엔티티 상호작용 이벤트
     */
    @EventHandler
    public void onRightClickAtPiece(@NonNull PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            gameService.inspectPiece(event.getPlayer(), event.getRightClicked());
        }
    }
}
