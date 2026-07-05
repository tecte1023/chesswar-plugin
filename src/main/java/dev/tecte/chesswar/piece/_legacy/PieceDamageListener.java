/*
package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.CombatManager;
import dev.tecte.chesswar.game.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@RequiredArgsConstructor
public class PieceDamageListener implements Listener {
    private final GameManager gameManager;
    private final CombatManager combatManager;
    private final PieceManager pieceManager;
    private final PiecePdcMapper pdcMapper;

    @EventHandler
    public void onDamage(final EntityDamageEvent event) {
        if (gameManager.phase() != GamePhase.BATTLE) {
            return;
        }

        final boolean isPiece = pieceManager.findCoordinate(event.getEntity()) != null;

        if (isPiece) {
            if (!combatManager.isProcessingAttack()) {
                event.setCancelled(true);
            }
            return;
        }

        if (event.getEntity() instanceof final Player player) {
            if (!combatManager.canTakeDamage(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAttack(final EntityDamageByEntityEvent event) {
        if (gameManager.phase() != GamePhase.BATTLE) {
            return;
        }

        if (combatManager.isProcessingAttack()) {
            return;
        }

        if (!(event.getDamager() instanceof final Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof final LivingEntity victim)) {
            return;
        }

        if (attacker.hasPermission("chesswar.admin") && !PieceItemUtils.isPieceItem(attacker.getInventory().getItemInMainHand())) {
            return;
        }

        if (!PieceItemUtils.isPieceItem(attacker.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            return;
        }

        if (pieceManager.findCoordinate(victim) == null) {
            return;
        }

        event.setCancelled(true);
        gameManager.attackPiece(attacker, victim);
    }
}
*/
