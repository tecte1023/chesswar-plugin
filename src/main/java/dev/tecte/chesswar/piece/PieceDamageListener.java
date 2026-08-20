package dev.tecte.chesswar.piece;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class PieceDamageListener implements Listener {
    @NotNull
    private final PieceManager pieceManager;

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(@NotNull final EntityDamageEvent event) {
        if (pieceManager.isDamageProtected(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

