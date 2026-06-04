package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.game.manager.CombatManager;
import dev.tecte.chesswar.game.manager.GameManager;
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

    @EventHandler
    public void onDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player participant)) {
            return;
        }

        if (!combatManager.canTakeDamage(participant)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof final Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof final LivingEntity victim)) {
            return;
        }

        event.setCancelled(true);
        gameManager.attackPiece(attacker, victim);
    }
}
