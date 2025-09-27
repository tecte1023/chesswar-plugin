package dev.tecte.chessWar.team.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
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
 * 팀과 관련된 모든 플레이어 명령어를 처리하는 클래스입니다.
 */
@Singleton
@CommandAlias(CommandConstants.ROOT)
@Subcommand("team")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class TeamCommand extends BaseCommand {
    private final TeamService teamService;

    /**
     * 플레이어가 특정 팀에 참가하는 명령어입니다.
     *
     * @param player    명령어를 실행한 플레이어
     * @param teamColor 참가할 팀의 색상 (not_full 조건에 의해 가득 차지 않은 팀만 가능)
     */
    @Subcommand("join")
    @Description("(흑/백) 팀에 참가합니다.")
    @CommandCompletion("@teamcolors")
    public void onTeamJoin(@NonNull Player player, @Conditions("not_full") @NonNull TeamColor teamColor) {
        teamService.joinTeam(player.getUniqueId(), teamColor);
        player.sendMessage("§a" + teamColor.getName() + "팀에 참가하셨습니다.");
    }
}
