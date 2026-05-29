package dev.tecte.chesswar.game.listener;

import dev.tecte.chesswar.piece.PieceManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@RequiredArgsConstructor
public class GameEntityListener implements Listener {
    private final PieceManager pieceManager;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        pieceManager.handlePieceDeath(event.getEntity());
    }
}
