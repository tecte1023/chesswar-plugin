package dev.tecte.chesswar.game;

import dev.tecte.chesswar.core.Messages;
import dev.tecte.chesswar.piece.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

@RequiredArgsConstructor
public final class TeamListener implements Listener {
    private final GameLifecycle gameLifecycle;
    private final TeamRegistry teamRegistry;

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (!gameLifecycle.isWaiting()) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Team woolTeam = resolveTeam(event.getMaterial());
        if (woolTeam == null) {
            return;
        }

        Player player = event.getPlayer();
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handleJoin(player, woolTeam);
            return;
        }

        if (player.isSneaking() && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
            if (!tryLeave(player, woolTeam)) {
                String teamName = formatTeamName(woolTeam);
                Messages.reply(player, Component.text(teamName + " 진영에 참가 중이 아닙니다.", NamedTextColor.RED));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!gameLifecycle.isWaiting()) {
            return;
        }

        Team woolTeam = resolveTeam(event.getBlock().getType());
        if (woolTeam == null) {
            return;
        }

        tryLeave(event.getPlayer(), woolTeam);
    }

    private void handleJoin(final Player player, final Team team) {
        TeamJoinResult result = teamRegistry.join(player.getUniqueId(), team);
        if (result == TeamJoinResult.SUCCESS) {
            String teamName = formatTeamName(team);
            Messages.reply(player, Component.text(teamName + " 진영에 참가했습니다.", NamedTextColor.GREEN));
        } else if (result == TeamJoinResult.TEAM_FULL) {
            String teamName = formatTeamName(team);
            Messages.reply(player, Component.text(teamName + " 진영 인원이 가득 찼습니다.", NamedTextColor.RED));
        }
    }

    private boolean tryLeave(final Player player, final Team team) {
        if (!teamRegistry.leave(player.getUniqueId(), team)) {
            return false;
        }

        String teamName = formatTeamName(team);
        Messages.reply(player, Component.text(teamName + " 진영에서 탈퇴했습니다.", NamedTextColor.YELLOW));

        return true;
    }

    private Team resolveTeam(final Material material) {
        if (material == Material.WHITE_WOOL) {
            return Team.WHITE;
        }

        if (material == Material.BLACK_WOOL) {
            return Team.BLACK;
        }

        return null;
    }

    private String formatTeamName(final Team team) {
        return team == Team.WHITE ? "백" : "흑";
    }
}
