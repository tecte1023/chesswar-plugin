package dev.tecte.chessWar.team.infrastructure.bukkit;

import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import dev.tecte.chessWar.team.domain.policy.TeamPolicy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ScoreboardTeamRepository implements TeamRepository {
    private static final String MAX_PLAYERS_OBJECTIVE = "chesswar_max_players";

    private final TeamPolicy teamPolicy;
    private final Scoreboard scoreboard;

    @Override
    public int getSize(@NonNull TeamColor teamColor) {
        Team team = scoreboard.getTeam(teamColor.name());

        return team == null ? 0 : team.getEntries().size();
    }

    @Override
    public int getMaxPlayers(@NonNull TeamColor teamColor) {
        return Optional.ofNullable(scoreboard.getObjective(MAX_PLAYERS_OBJECTIVE))
                .map(obj -> obj.getScore(teamColor.name()))
                .filter(Score::isScoreSet)
                .map(Score::getScore)
                .map(teamPolicy::applyTo)
                .orElseGet(teamPolicy::getDefaultMaxPlayers);
    }

    @Override
    public void addPlayer(@NonNull UUID playerId, @NonNull TeamColor teamColor) {
        Team team = Optional.ofNullable(scoreboard.getTeam(teamColor.name()))
                .orElseGet(() -> create(teamColor));

        team.addEntry(playerId.toString());
    }

    @NonNull
    private Team create(@NonNull TeamColor color) {
        Team team = scoreboard.registerNewTeam(color.name().toLowerCase());

        team.setAllowFriendlyFire(false);
        team.setCanSeeFriendlyInvisibles(true);
        team.displayName(Component.text(color.getName()));
        team.color(color.getTextColor());

        return team;
    }
}
