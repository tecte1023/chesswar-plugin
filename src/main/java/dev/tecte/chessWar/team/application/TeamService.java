package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.exception.TeamException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final TeamRepository teamRepository;
    private final SenderNotifier senderNotifier;
    private final JavaPlugin plugin;

    /**
     * 플레이어를 지정된 색상의 팀에 참가시킵니다.
     *
     * @param player    팀에 참가할 플레이어
     * @param teamColor 참가할 팀의 색상
     */
    @HandleException
    public void joinTeam(@NonNull Player player, @NonNull TeamColor teamColor) {
        checkTeamCapacity(teamColor);
        teamRepository.addPlayer(player.getUniqueId(), teamColor);

        Component successMessage = Component.text(teamColor.displayName(), teamColor.textColor())
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

    /**
     * 지정된 색상 팀에 속한 모든 온라인 플레이어를 조회합니다.
     *
     * @param teamColor 조회할 팀의 색상
     * @return 해당 팀에 속한 모든 온라인 플레이어의 집합
     */
    @NonNull
    public Set<Player> getOnlinePlayers(@NonNull TeamColor teamColor) {
        return teamRepository.getPlayerUUIDs(teamColor).stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 모든 팀의 온라인 플레이어를 조회합니다.
     *
     * @return 모든 팀에 속한 온라인 플레이어의 집합
     */
    @NonNull
    public Set<Player> getAllOnlinePlayers() {
        Set<Player> allPlayers = new HashSet<>();

        for (TeamColor color : TeamColor.values()) {
            allPlayers.addAll(getOnlinePlayers(color));
        }

        return allPlayers;
    }

    /**
     * 적 팀 플레이어를 보이게 설정합니다.
     */
    public void revealEnemies() {
        applyEnemyVisibility(true);
    }

    /**
     * 적 팀 플레이어를 보이지 않게 설정합니다.
     */
    public void concealEnemies() {
        applyEnemyVisibility(false);
    }

    /**
     * 특정 플레이어에게 적 팀 플레이어들을 보이지 않게 숨깁니다.
     *
     * @param player     대상 플레이어
     * @param playerTeam 대상 플레이어의 팀
     */
    public void concealEnemiesFor(@NonNull Player player, @NonNull TeamColor playerTeam) {
        Set<Player> enemyPlayers = getOnlinePlayers(playerTeam.opposite());

        for (Player enemyPlayer : enemyPlayers) {
            player.hidePlayer(plugin, enemyPlayer);
            enemyPlayer.hidePlayer(plugin, player);
        }
    }

    /**
     * 플레이어가 소속된 팀을 찾습니다.
     *
     * @param player 조회할 플레이어
     * @return 플레이어가 속한 팀 색상을 담은 {@link Optional}, 소속 팀이 없으면 빈 {@link Optional}
     */
    @NonNull
    public Optional<TeamColor> findTeam(@NonNull Player player) {
        return teamRepository.findTeam(player.getUniqueId());
    }

    /**
     * 팀의 모든 온라인 플레이어를 지정된 위치로 이동시킵니다.
     *
     * @param teamColor 이동할 팀의 색상
     * @param location  이동할 위치
     */
    public void teleportTeam(@NonNull TeamColor teamColor, @NonNull Location location) {
        getOnlinePlayers(teamColor).forEach(player -> player.teleport(location));
    }

    private void checkTeamCapacity(@NonNull TeamColor teamColor) {
        if (teamRepository.getSize(teamColor) >= teamRepository.getMaxPlayers()) {
            throw TeamException.capacityExceeded(teamColor);
        }
    }

    private void applyEnemyVisibility(boolean canSeeEnemy) {
        Set<Player> whitePlayers = getOnlinePlayers(TeamColor.WHITE);
        Set<Player> blackPlayers = getOnlinePlayers(TeamColor.BLACK);

        for (Player whitePlayer : whitePlayers) {
            for (Player blackPlayer : blackPlayers) {
                if (canSeeEnemy) {
                    whitePlayer.showPlayer(plugin, blackPlayer);
                    blackPlayer.showPlayer(plugin, whitePlayer);
                } else {
                    whitePlayer.hidePlayer(plugin, blackPlayer);
                    blackPlayer.hidePlayer(plugin, whitePlayer);
                }
            }
        }
    }
}
