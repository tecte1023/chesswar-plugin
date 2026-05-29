package dev.tecte.chesswar.piece;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@RequiredArgsConstructor
public class PieceEntityLifecycleListener implements Listener {
    private final PieceManager pieceManager;

    @EventHandler
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        pieceManager.purgeEntity(event.getEntity());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        pieceManager.purgeEntity(event.getEntity());
    }
}
