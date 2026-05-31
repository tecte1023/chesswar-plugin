package dev.tecte.chesswar.game.listener;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.TurnStartedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CombatListener implements Listener {
    private final ChessWar plugin;

    public CombatListener(final ChessWar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTurnStart(final TurnStartedEvent event) {
        plugin.combatManager().updateInvulnerability(event.getPlayer());
    }
}
