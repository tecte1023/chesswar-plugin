package dev.tecte.chesswar.board;

import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GamePhase;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    public void onBlockBreak(BlockBreakEvent event) {
        if (boardManager.isBarracksChest(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        if (blockType == Material.WHITE_WOOL || blockType == Material.BLACK_WOOL) {
            if (gameManager.phase() != GamePhase.WAITING) {
                return;
            }

            if (gameManager.isParticipant(player)) {
                event.setCancelled(true);
                gameManager.leave(player);
                player.sendMessage(Component.text("팀에서 나갔습니다.", NamedTextColor.YELLOW));
            }
        }
    }
}
