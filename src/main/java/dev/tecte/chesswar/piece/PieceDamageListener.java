package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.CombatManager;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;


@RequiredArgsConstructor
public class PieceDamageListener implements Listener {
    private final CombatManager combatManager;

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }

        if (!combatManager.canTakeDamage(participant)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        event.setCancelled(true);
        combatManager.handleAttack(attacker, victim);
    }
}
