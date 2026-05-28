package dev.tecte.chesswar.board;

import dev.tecte.chesswar.game.GameResetEvent;
import dev.tecte.chesswar.game.TurnStartedEvent;
import dev.tecte.chesswar.piece.PieceItemUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import dev.tecte.chesswar.piece.PieceMoveEvent;

@RequiredArgsConstructor
public class BoardVisualGuideListener implements Listener {
    private final BoardVisualManager visualManager;

    @EventHandler
    public void onGameReset(GameResetEvent event) {
        visualManager.clearAllGuides();
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        visualManager.clearGuide(player);

        if (PieceItemUtils.isPieceItem(newItem)) {
            visualManager.showGuide(player);
        }
    }

    @EventHandler
    public void onPieceMove(PieceMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        visualManager.clearGuide(player);

        if (PieceItemUtils.isPieceItem(item)) {
            visualManager.showGuide(player);
        }
    }

    @EventHandler
    public void onTurnStarted(TurnStartedEvent event) {
        visualManager.clearAllGuides();

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (PieceItemUtils.isPieceItem(item)) {
            visualManager.showGuide(player);
        }
    }

    @EventHandler
    public void onCommandTargetSelected(BoardTargetSelectedEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        visualManager.clearGuide(player);

        if (PieceItemUtils.isPieceItem(item)) {
            visualManager.showGuide(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        visualManager.clearGuide(event.getPlayer());
    }
}
