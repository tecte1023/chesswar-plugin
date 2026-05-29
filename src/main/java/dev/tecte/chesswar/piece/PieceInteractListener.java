package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.CombatManager;
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
    private final CombatManager combatManager;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (gameManager.phase() == GamePhase.WAITING) {
            handleWaitingInteraction(player, item, event.getAction());
            return;
        }

        if (gameManager.phase() == GamePhase.BATTLE) {
            handleBattleInteraction(player, item, event.getAction());
        }
    }

    private void handleWaitingInteraction(Player player, ItemStack item, Action action) {
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
            player.sendMessage(Component.text(targetTeam.displayName() + "에 참가했습니다!", targetTeam.textColor()));
        }
    }

    private void handleBattleInteraction(Player player, ItemStack item, Action action) {
        if (!PieceItemUtils.isPieceItem(item)) {
            return;
        }

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        combatManager.handleMove(player);
    }
}
