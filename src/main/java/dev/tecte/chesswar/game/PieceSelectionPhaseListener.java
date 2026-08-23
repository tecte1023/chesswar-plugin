package dev.tecte.chesswar.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class PieceSelectionPhaseListener implements Listener {
    @NotNull
    private final PieceSelectionPhaseManager manager;

    @NotNull
    private final PieceSelectionPhasePresenter presenter;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(@NotNull final PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        final Player player = event.getPlayer();
        final PieceInspectionResult result = manager.tryInspectPiece(player, event.getRightClicked().getUniqueId());
        
        if (result == null) {
            return;
        }
        
        presenter.showPieceDescription(player, result);
    }
}
