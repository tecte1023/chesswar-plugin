package dev.tecte.chesswar.game.listener;

import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

@RequiredArgsConstructor
public class GameSelectionListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onPieceInspect(final PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (gameManager.inspectBarracksPiece(event.getPlayer(), event.getRightClicked())) {
            event.setCancelled(true);
        }
    }
}
