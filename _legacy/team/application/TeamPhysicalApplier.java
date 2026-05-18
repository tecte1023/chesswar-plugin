package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.policy.TeamFacingPolicy;
import dev.tecte.chessWar.port.WorldResolver;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Set;

/**
 * 팀원에 대한 물리적인 조작을 담당합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamPhysicalApplier {
    private final TeamFacingPolicy teamFacingPolicy;
    private final TeamService teamService;
    private final WorldResolver worldResolver;
    private final JavaPlugin plugin;

    /**
     * 모든 플레이어를 시작 위치로 배치합니다.
     *
     * @param board 체스판
     */
    public void deployAllTeams(@NonNull Board board) {
        String worldName = board.worldName();

        for (TeamColor color : TeamColor.values()) {
            deployTeam(
                    color,
                    board.startingPositionOf(color),
                    teamFacingPolicy.ownCampFacingOf(color, board),
                    worldName
            );
        }
    }

    /**
     * 팀 플레이어를 지정된 위치로 이동시킵니다.
     *
     * @param teamColor 대상 팀
     * @param position  이동 좌표
     * @param direction 바라볼 방향
     * @param worldName 월드 이름
     */
    public void deployTeam(
            @NonNull TeamColor teamColor,
            @NonNull Vector position,
            @NonNull Vector direction,
            @NonNull String worldName
    ) {
        World world = worldResolver.resolve(worldName, GameException::worldNotFound);
        Location targetLocation = position.toLocation(world);
        targetLocation.setDirection(direction);

        teamService.findPlayers(teamColor).forEach(player -> player.teleport(targetLocation));
    }

    /**
     * 적 팀 플레이어를 드러냅니다.
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
     * 적 팀 플레이어를 숨깁니다.
     *
     * @param player     대상 플레이어
     * @param playerTeam 소속 팀
     */
    public void concealEnemiesFor(@NonNull Player player, @NonNull TeamColor playerTeam) {
        Set<Player> enemies = teamService.findPlayers(playerTeam.opposite());

        for (Player enemy : enemies) {
            player.hidePlayer(plugin, enemy);
            enemy.hidePlayer(plugin, player);
        }
    }

    private void applyEnemyVisibility(boolean canSeeEnemy) {
        Set<Player> whitePlayers = teamService.findPlayers(TeamColor.WHITE);
        Set<Player> blackPlayers = teamService.findPlayers(TeamColor.BLACK);

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
