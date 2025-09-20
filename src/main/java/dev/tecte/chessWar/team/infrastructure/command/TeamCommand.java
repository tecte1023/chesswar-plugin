package dev.tecte.chessWar.team.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import dev.tecte.chessWar.team.application.TeamService;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Singleton
@CommandAlias(CommandConstants.ROOT)
@Subcommand("team")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class TeamCommand extends BaseCommand {
    private final TeamService teamService;

    @Subcommand("join")
    @Description("(흑/백) 팀에 참가합니다.")
    @CommandCompletion("@teamcolors")
    public void onTeamJoin(@NonNull Player player, @NonNull TeamColor teamColor) {
        teamService.joinTeam(player.getUniqueId(), teamColor);
        player.sendMessage("§a" + teamColor.getName() + "팀에 참가하셨습니다.");
    }

    @HelpCommand
    public void onHelp(@NonNull CommandHelp help) {
        help.showHelp();
    }
}
