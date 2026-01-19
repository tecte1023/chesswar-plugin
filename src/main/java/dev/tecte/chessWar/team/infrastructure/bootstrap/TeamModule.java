package dev.tecte.chessWar.team.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.infrastructure.command.CommandConfigurer;
import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.model.TeamCapacityPolicy;
import dev.tecte.chessWar.team.infrastructure.command.TeamCommand;
import dev.tecte.chessWar.team.infrastructure.command.TeamCommandConfigurer;
import dev.tecte.chessWar.team.infrastructure.listener.TeamJoinListener;
import dev.tecte.chessWar.team.infrastructure.persistence.ScoreboardTeamRepository;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;

/**
 * 팀 도메인의 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 * 팀의 정책, 저장소, 리스너, 명령어 설정자 등 관련된 모든 컴포넌트의 바인딩을 관리합니다.
 */
public class TeamModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TeamCapacityPolicy.class).toInstance(TeamCapacityPolicy.defaultPolicy());
        bind(TeamRepository.class).to(ScoreboardTeamRepository.class);
        bind(Scoreboard.class).toInstance(Bukkit.getScoreboardManager().getMainScoreboard());

        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(TeamCommand.class);
        Multibinder.newSetBinder(binder(), CommandConfigurer.class).addBinding().to(TeamCommandConfigurer.class);
        Multibinder.newSetBinder(binder(), Listener.class).addBinding().to(TeamJoinListener.class);
    }
}
