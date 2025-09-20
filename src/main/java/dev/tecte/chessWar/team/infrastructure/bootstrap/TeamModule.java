package dev.tecte.chessWar.team.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.infrastructure.command.CommandConfigurer;
import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.policy.DefaultTeamPolicy;
import dev.tecte.chessWar.team.domain.policy.TeamPolicy;
import dev.tecte.chessWar.team.infrastructure.bukkit.ScoreboardTeamRepository;
import dev.tecte.chessWar.team.infrastructure.command.TeamCommandConfigurer;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;

public class TeamModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TeamPolicy.class).to(DefaultTeamPolicy.class);
        bind(TeamRepository.class).to(ScoreboardTeamRepository.class);
        bind(Scoreboard.class).toInstance(Bukkit.getScoreboardManager().getMainScoreboard());

        // Team 도메인의 커맨드 설정을 중앙 CommandModule에 동적으로 추가하기 위해 Multibinder에 등록
        Multibinder<CommandConfigurer> configurerBinder = Multibinder.newSetBinder(binder(), CommandConfigurer.class);

        configurerBinder.addBinding().to(TeamCommandConfigurer.class);
    }
}
