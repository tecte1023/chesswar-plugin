package dev.tecte.chesswar.team;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class TeamSelectionListener implements Listener {
    @NotNull
    private final TeamManager teamManager;

    @EventHandler
    public void onPlayerInteract(@NotNull final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        final Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        final TeamSide targetTeamSide = TeamSide.fromMaterial(item.getType());

        if (targetTeamSide == null) {
            return;
        }

        teamManager.tryJoinTeam(event.getPlayer(), targetTeamSide);
    }

    @EventHandler
    public void onBlockBreak(@NotNull final BlockBreakEvent event) {
        final TeamSide targetTeamSide = TeamSide.fromMaterial(event.getBlock().getType());

        if (targetTeamSide == null) {
            return;
        }

        teamManager.tryLeaveTeam(event.getPlayer(), targetTeamSide);
    }
}
