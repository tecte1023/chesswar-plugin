package dev.tecte.chesswar.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.tecte.chesswar.team.JoinResult;
import dev.tecte.chesswar.team.LeaveResult;
import dev.tecte.chesswar.team.TeamManager;
import dev.tecte.chesswar.team.TeamPresenter;
import dev.tecte.chesswar.team.TeamSide;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("chesswar|cw")
@Subcommand("admin team")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
public final class TeamAdminCommand extends BaseCommand {
    @NotNull
    private final TeamManager teamManager;

    @NotNull
    private final TeamPresenter teamPresenter;

    @Subcommand("list")
    public void onListTeams(@NotNull final CommandSender sender) {
        teamPresenter.showTeamList(sender, teamManager.teamRosters());
    }

    @Subcommand("add")
    public void onAddPlayerToTeam(
            @NotNull final CommandSender sender,
            @NotNull final OnlinePlayer target,
            @NotNull final TeamSide teamSide
    ) {
        final Player targetPlayer = target.getPlayer();
        final JoinResult result = teamManager.tryJoinTeam(targetPlayer, teamSide);

        teamPresenter.showAdminJoinFeedback(sender, targetPlayer, teamSide, result);
    }

    @Subcommand("leave")
    public void onLeavePlayerFromTeam(
            @NotNull final CommandSender sender,
            @NotNull final OnlinePlayer target,
            @NotNull final TeamSide teamSide
    ) {
        final Player targetPlayer = target.getPlayer();
        final LeaveResult result = teamManager.tryLeaveTeam(targetPlayer, teamSide);

        teamPresenter.showAdminLeaveFeedback(sender, targetPlayer, teamSide, result);
    }
}
