package dev.tecte.chesswar.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class PieceSelectionPhaseListener implements Listener {
    @NotNull
    private final PieceSelectionPhaseManager manager;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(@NotNull final PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        manager.inspectPiece(event.getPlayer(), event.getRightClicked().getUniqueId());
    }
}
