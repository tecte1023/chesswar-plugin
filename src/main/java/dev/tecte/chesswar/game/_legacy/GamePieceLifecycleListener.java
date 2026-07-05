/*
package dev.tecte.chesswar.game;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@RequiredArgsConstructor
public class GamePieceLifecycleListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onPieceRemove(final EntityRemoveFromWorldEvent event) {
        gameManager.processPieceUnload(event.getEntity());
    }

    @EventHandler
    public void onPieceDeath(final EntityDeathEvent event) {
        gameManager.processPieceDeath(event.getEntity());
    }
}
*/
