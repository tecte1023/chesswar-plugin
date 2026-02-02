package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.exception.TeamException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
 * 팀 관련 비즈니스 로직을 처리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamService {
    private final TeamNotifier teamNotifier;
    private final TeamRepository teamRepository;
    private final JavaPlugin plugin;

    /**
     * 플레이어를 팀에 참가시킵니다.
     *
     * @param player    참가할 플레이어
     * @param teamColor 참가할 팀의 색상
     */
    @HandleException
    public void joinTeam(@NonNull Player player, @NonNull TeamColor teamColor) {
        checkTeamCapacity(teamColor);
        teamRepository.addPlayer(player.getUniqueId(), teamColor);
        teamNotifier.notifyJoin(player, teamColor);
    }

    /**
     * 플레이어를 팀에서 나가게 합니다.
     *
     * @param player 나갈 플레이어
     */
    @HandleException
    public void leaveTeam(@NonNull Player player) {
        if (!teamRepository.removePlayer(player.getUniqueId())) {
            throw TeamException.notInTeam();
        }

        teamNotifier.notifyLeave(player);
    }

    /**
     * 모든 팀이 최소 인원을 충족하는지 확인합니다.
     *
     * @param minPlayers 팀별 최소 인원
     * @return 충족 여부
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
     * 해당 팀에서 접속한 모든 플레이어를 찾습니다.
     *
     * @param teamColor 찾을 팀의 색상
     * @return 접속한 플레이어 목록
     */
    @NonNull
    public Set<Player> getOnlinePlayers(@NonNull TeamColor teamColor) {
        return teamRepository.getPlayerUUIDs(teamColor).stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 모든 팀에서 접속한 모든 플레이어를 찾습니다.
     *
     * @return 접속한 모든 플레이어 목록
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
     * 적 팀 플레이어를 보이게 합니다.
     */
    public void revealEnemies() {
        applyEnemyVisibility(true);
    }

    /**
     * 적 팀 플레이어를 숨깁니다.
     */
    public void concealEnemies() {
        applyEnemyVisibility(false);
    }

    /**
     * 플레이어에게 적 팀을 숨깁니다.
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
     * 플레이어가 속한 팀을 찾습니다.
     *
     * @param player 찾을 플레이어
     * @return 찾은 팀의 색상
     */
    @NonNull
    public Optional<TeamColor> findTeam(@NonNull Player player) {
        return teamRepository.findTeam(player.getUniqueId());
    }

    /**
     * 팀원들을 해당 위치로 이동시킵니다.
     *
     * @param teamColor 이동할 팀
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
