package dev.tecte.chessWar.team.infrastructure.persistence;

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
    private static final String MAX_PLAYERS_OBJECTIVE = PREFIX + "team";
    private static final String MAX_PLAYERS_ENTRY = PREFIX + "max_players";

    private final TeamCapacityPolicy teamPolicy;
    private final Scoreboard scoreboard;

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize(@NonNull TeamColor teamColor) {
        Team team = scoreboard.getTeam(getTeamName(teamColor));

        return team == null ? 0 : team.getEntries().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxPlayers() {
        Score score = getMaxPlayersScore();

        // 스코어보드에 최대 인원 수가 설정되지 않은 경우, 정책의 기본값으로 초기화
        if (!score.isScoreSet()) {
            int maxPlayers = TeamCapacityPolicy.MAX_PLAYERS_DEFAULT;

            log.info("Max players not set, initializing to default value({}).", maxPlayers);

            return persistMaxPlayers(maxPlayers);
        }

        int unsafeValue = score.getScore();
        int safeValue = teamPolicy.applyTo(unsafeValue);

        // 저장된 값이 정책을 위반하는 경우, 정책을 적용한 값으로 다시 저장
        return unsafeValue == safeValue ? safeValue : setMaxPlayers(unsafeValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int setMaxPlayers(int maxPlayers) {
        int appliedValue = teamPolicy.applyTo(maxPlayers);

        if (maxPlayers != appliedValue) {
            log.warn("Invalid max player count found ({}). Value must be between {}-{}. Resetting to {}.",
                    maxPlayers, TeamCapacityPolicy.MAX_PLAYERS_LOWER_BOUND, TeamCapacityPolicy.MAX_PLAYERS_UPPER_BOUND, appliedValue);
        }

        return persistMaxPlayers(appliedValue);
    }

    /**
     * {@inheritDoc}
     */
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
                Component.text("ChessWar Team")
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
