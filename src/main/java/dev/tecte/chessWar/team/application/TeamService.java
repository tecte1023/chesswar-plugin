package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import dev.tecte.chessWar.team.domain.policy.TeamMembershipPolicy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * 팀 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 플레이어의 팀 참가, 탈퇴 등과 같은 유스케이스를 담당합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamService {
    private static final Component TEAM_JOIN_SUCCESS = Component.text(
            "에 참가했습니다.",
            NamedTextColor.AQUA
    );

    private final TeamMembershipPolicy teamMembershipPolicy;
    private final TeamRepository teamRepository;
    private final SenderNotifier senderNotifier;

    /**
     * 플레이어를 지정된 색상의 팀에 참가시킵니다。
     *
     * @param player    팀에 참가할 플레이어
     * @param teamColor 참가할 팀의 색상
     */
    @HandleException
    public void joinTeam(@NonNull Player player, @NonNull TeamColor teamColor) {
        teamMembershipPolicy.checkIfJoinable(teamColor);
        teamRepository.addPlayer(player.getUniqueId(), teamColor);

        Component successMessage = Component.text(teamColor.getName(), teamColor.getTextColor())
                .append(TEAM_JOIN_SUCCESS);

        senderNotifier.notifySuccess(player, successMessage);
    }

    /**
     * 모든 팀이 최소 플레이어 수를 충족하는지 확인합니다.
     *
     * @param minPlayers 각 팀이 충족해야 할 최소 플레이어 수
     * @return 모든 팀이 최소 플레이어 수를 충족하면 true, 그렇지 않으면 false
     */
    public boolean areAllTeamsReadyToStart(int minPlayers) {
        for (TeamColor color : TeamColor.values()) {
            if (teamRepository.getSize(color) < minPlayers) {
                return false;
            }
        }

        return true;
    }
}
