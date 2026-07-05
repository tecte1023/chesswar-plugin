/*
package dev.tecte.chesswar.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

@RequiredArgsConstructor
public class EnvironmentListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onHungerChange(final FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();

        if (gameManager.isHungerProtected(player)) {
            event.setCancelled(true);
        }
    }
}
*/
