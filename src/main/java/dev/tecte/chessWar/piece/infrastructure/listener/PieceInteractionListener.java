package dev.tecte.chessWar.piece.infrastructure.listener;

import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.game.domain.event.PieceInspectionRequestedEvent;
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
 * 기물과의 물리적 상호작용을 도메인 이벤트로 전환합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceInteractionListener implements Listener {
    private final DomainEventDispatcher eventDispatcher;

    /**
     * 플레이어의 클릭 행위를 도메인의 조사 요청으로 전환합니다.
     *
     * @param event 상호작용 이벤트
     */
    @EventHandler
    public void onInteract(@NonNull PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getRightClicked() instanceof Player) {
            return;
        }

        eventDispatcher.dispatch(PieceInspectionRequestedEvent.of(
                event.getPlayer().getUniqueId(),
                event.getRightClicked().getUniqueId()
        ));
    }
}
