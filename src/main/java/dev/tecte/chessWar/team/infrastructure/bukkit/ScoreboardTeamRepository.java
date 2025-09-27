package dev.tecte.chessWar.team.infrastructure.bukkit;

import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import dev.tecte.chessWar.team.domain.policy.TeamCapacityPolicy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Optional;
import java.util.UUID;

/**
 * {@link TeamRepository}의 Bukkit Scoreboard API 기반 구현체입니다.
 * 팀 데이터를 Minecraft Scoreboard에 저장하고 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ScoreboardTeamRepository implements TeamRepository {
    private static final String PREFIX = "cw_";
    private static final String MAX_PLAYERS_ENTRY = PREFIX + "max_players";
    private static final String MAX_PLAYERS_OBJECTIVE = PREFIX + MAX_PLAYERS_ENTRY;

    private final TeamCapacityPolicy teamPolicy;
    private final Scoreboard scoreboard;

    @Override
    public int getSize(@NonNull TeamColor teamColor) {
        Team team = scoreboard.getTeam(getTeamName(teamColor));

        return team == null ? 0 : team.getEntries().size();
    }

    @Override
    public int getMaxPlayers() {
        Score score = getMaxPlayersScore();

        if (!score.isScoreSet()) {
            int maxPlayers = TeamCapacityPolicy.MAX_PLAYERS_DEFAULT;

            log.info("Max players not set, initializing to default value({}).", maxPlayers);

            return persistMaxPlayers(maxPlayers);
        }

        int unsafeValue = score.getScore();
        int safeValue = teamPolicy.applyTo(unsafeValue);

        return unsafeValue == safeValue ? safeValue : setMaxPlayers(unsafeValue);
    }

    @Override
    public int setMaxPlayers(int maxPlayers) {
        int appliedValue = teamPolicy.applyTo(maxPlayers);

        if (maxPlayers != appliedValue) {
            log.warn("Invalid max player count found ({}). Value must be between {}-{}. Resetting to {}.",
                    maxPlayers, TeamCapacityPolicy.MAX_PLAYERS_LOWER_BOUND, TeamCapacityPolicy.MAX_PLAYERS_UPPER_BOUND, appliedValue);
        }

        return persistMaxPlayers(appliedValue);
    }

    @Override
    public void addPlayer(@NonNull UUID playerId, @NonNull TeamColor teamColor) {
        Team team = Optional.ofNullable(scoreboard.getTeam(getTeamName(teamColor)))
                .orElseGet(() -> createTeam(teamColor));

        team.addEntry(playerId.toString());
    }

    @NonNull
    private String getTeamName(@NonNull TeamColor teamColor) {
        return PREFIX + teamColor.name().toLowerCase();
    }

    @NonNull
    private Score getMaxPlayersScore() {
        Objective objective = Optional.ofNullable(scoreboard.getObjective(MAX_PLAYERS_OBJECTIVE))
                .orElseGet(this::createObjective);

        return objective.getScore(MAX_PLAYERS_ENTRY);
    }

    @NonNull
    private Objective createObjective() {
        return scoreboard.registerNewObjective(
                MAX_PLAYERS_OBJECTIVE,
                Criteria.DUMMY,
                Component.text("ChessWar Max Players")
        );
    }

    @NonNull
    private Team createTeam(@NonNull TeamColor teamColor) {
        Team team = scoreboard.registerNewTeam(getTeamName(teamColor));

        team.setAllowFriendlyFire(false);
        team.setCanSeeFriendlyInvisibles(true);
        team.displayName(Component.text(teamColor.getName()));
        team.color(teamColor.getTextColor());

        return team;
    }

    private int persistMaxPlayers(int value) {
        getMaxPlayersScore().setScore(value);

        return value;
    }
}
