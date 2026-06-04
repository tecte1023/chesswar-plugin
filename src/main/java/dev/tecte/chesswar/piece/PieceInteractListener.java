package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PieceInteractListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (gameManager.phase() == GamePhase.WAITING) {
            handleWaitingInteraction(player, item, event.getAction());
            return;
        }

        if (gameManager.phase() == GamePhase.BATTLE) {
            handleBattleInteraction(player, item, event.getAction());
        }
    }

    private void handleWaitingInteraction(final Player player, final ItemStack item, final Action action) {
        if (item == null || (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Team targetTeam = null;
        if (item.getType() == Material.WHITE_WOOL) {
            targetTeam = Team.WHITE;
        } else if (item.getType() == Material.BLACK_WOOL) {
            targetTeam = Team.BLACK;
        }

        if (targetTeam != null) {
            gameManager.join(player, targetTeam);
            player.sendMessage(Component.text(targetTeam.teamName() + "에 참가했습니다!", targetTeam.color()));
        }
    }

    private void handleBattleInteraction(final Player player, final ItemStack item, final Action action) {
        if (!PieceItemUtils.isPieceItem(item)) {
            return;
        }

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        gameManager.movePiece(player);
    }
}
