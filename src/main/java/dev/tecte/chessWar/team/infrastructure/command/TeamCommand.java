package dev.tecte.chessWar.team.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import dev.tecte.chessWar.team.application.TeamService;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 * 팀 관련 명령어를 처리하는 클래스입니다.
 */
@Singleton
@CommandAlias(CommandConstants.ROOT_ALIAS)
@Subcommand("team")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class TeamCommand extends BaseCommand {
    private final TeamService teamService;

    /**
     * 플레이어가 특정 팀에 참가합니다.
     *
     * @param player    명령어를 실행한 플레이어
     * @param teamColor 참가할 팀의 색상
     */
    @Subcommand("join")
    @Description("(흑팀/백팀)에 참가합니다.")
    @CommandCompletion("@teamcolors")
    public void join(@NonNull Player player, @NonNull TeamColor teamColor) {
        teamService.joinTeam(player, teamColor);
    }
}
