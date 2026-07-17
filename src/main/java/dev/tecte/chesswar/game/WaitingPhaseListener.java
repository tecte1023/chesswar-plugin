package dev.tecte.chesswar.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class WaitingPhaseListener implements Listener {
    @NotNull
    private final WaitingPhaseManager waitingPhaseManager;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(@NotNull final PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) {
            return;
        }

        waitingPhaseManager.tryStartGame(interaction.getUniqueId());
    }
}
