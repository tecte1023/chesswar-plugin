package dev.tecte.chesswar.board;

import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@RequiredArgsConstructor
public class BoardBlockListener implements Listener {
    private final GameManager gameManager;
    private final BoardManager boardManager;

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (boardManager.isBarracksChest(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        final Player player = event.getPlayer();
        final Material blockType = event.getBlock().getType();

        if (blockType != Material.WHITE_WOOL && blockType != Material.BLACK_WOOL) {
            return;
        }

        gameManager.processWoolBreakLeave(player, blockType);
    }
}
