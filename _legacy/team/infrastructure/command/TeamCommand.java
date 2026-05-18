package dev.tecte.chessWar.team.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.infrastructure.command.CommandRouting;
import dev.tecte.chessWar.team.application.TeamService;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 * 팀 관련 명령어를 처리합니다.
 */
@Singleton
@CommandAlias(CommandRouting.ROOT_ALIAS)
@Subcommand("team")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class TeamCommand extends BaseCommand {
    private final TeamService teamService;

    /**
     * 팀에 참가합니다.
     *
     * @param player    행위자
     * @param teamColor 참가할 팀
     */
    @Subcommand("join")
    @Description("(흑팀/백팀)에 참가합니다.")
    @CommandCompletion("@teamcolors")
    public void join(@NonNull Player player, @NonNull TeamColor teamColor) {
        teamService.joinTeam(player, teamColor);
    }

    /**
     * 팀에서 나갑니다.
     *
     * @param player 행위자
     */
    @Subcommand("leave")
    @Description("팀에서 나갑니다.")
    public void leave(@NonNull Player player) {
        teamService.leaveTeam(player);
    }
}
