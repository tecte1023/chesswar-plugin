package dev.tecte.chesswar.listener;

import dev.tecte.chesswar.game.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@RequiredArgsConstructor
public class ChessBlockListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (gameManager.isBarracksChest(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
