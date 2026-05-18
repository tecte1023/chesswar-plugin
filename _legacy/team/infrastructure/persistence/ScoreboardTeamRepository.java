package dev.tecte.chessWar.team.infrastructure.persistence;

import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.model.TeamColor;
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
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Scoreboard 기반으로 팀 정보를 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ScoreboardTeamRepository implements TeamRepository {
    private static final String PREFIX = "cw_";
    private static final String CAPACITY_OBJECTIVE = PREFIX + "team";
    private static final String CAPACITY_ENTRY = PREFIX + "max_capacity";

    private final Scoreboard scoreboard;

    @Override
    public OptionalInt findMaxCapacity() {
        Score score = getCapacityScore();

        return score.isScoreSet() ? OptionalInt.of(score.getScore()) : OptionalInt.empty();
    }

    @Override
    public void saveMaxCapacity(int capacity) {
        getCapacityScore().setScore(capacity);
    }

    @Override
    public void addPlayer(@NonNull UUID playerId, @NonNull TeamColor teamColor) {
        Team team = scoreboard.getTeam(getTeamName(teamColor));

        if (team == null) {
            team = registerTeam(teamColor);
        }

        team.addEntry(playerId.toString());
    }

    @Override
    public boolean removePlayer(@NonNull UUID playerId) {
        Team team = scoreboard.getEntryTeam(playerId.toString());

        if (team == null || parseTeamColor(team.getName()).isEmpty()) {
            return false;
        }

        return team.removeEntry(playerId.toString());
    }

    @NonNull
    @Override
    public Optional<TeamColor> findTeam(@NonNull UUID playerId) {
        return Optional.ofNullable(scoreboard.getEntryTeam(playerId.toString()))
                .map(Team::getName)
                .flatMap(this::parseTeamColor);
    }

    @Override
    public int countPlayers(@NonNull TeamColor teamColor) {
        Team team = scoreboard.getTeam(getTeamName(teamColor));

        return team == null ? 0 : team.getEntries().size();
    }

    @NonNull
    @Override
    public Set<UUID> findAllPlayerIds(@NonNull TeamColor teamColor) {
        Team team = scoreboard.getTeam(getTeamName(teamColor));

        if (team == null) {
            return Set.of();
        }

        return team.getEntries().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    @NonNull
    private Score getCapacityScore() {
        Objective objective = scoreboard.getObjective(CAPACITY_OBJECTIVE);

        if (objective == null) {
            objective = createObjective();
        }

        return objective.getScore(CAPACITY_ENTRY);
    }

    @NonNull
    private Objective createObjective() {
        return scoreboard.registerNewObjective(
                CAPACITY_OBJECTIVE,
                Criteria.DUMMY,
                Component.text("ChessWar Team")
        );
    }

    @NonNull
    private Team registerTeam(@NonNull TeamColor teamColor) {
        Team team = scoreboard.registerNewTeam(getTeamName(teamColor));

        configureTeam(team, teamColor);

        return team;
    }

    private void configureTeam(@NonNull Team team, @NonNull TeamColor teamColor) {
        team.displayName(Component.text(teamColor.displayName()));
        team.color(teamColor.textColor());
        team.setAllowFriendlyFire(false);
        team.setCanSeeFriendlyInvisibles(true);
    }

    @NonNull
    private String getTeamName(@NonNull TeamColor teamColor) {
        return PREFIX + teamColor.name().toLowerCase();
    }

    @NonNull
    private Optional<TeamColor> parseTeamColor(@NonNull String teamName) {
        return Optional.of(teamName)
                .filter(name -> name.startsWith(PREFIX))
                .map(name -> name.substring(PREFIX.length()))
                .flatMap(TeamColor::from);
    }
}
