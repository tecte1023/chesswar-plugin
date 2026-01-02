package dev.tecte.chessWar.game.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.common.persistence.PersistableState;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.application.port.GameTaskScheduler;
import dev.tecte.chessWar.game.infrastructure.bukkit.BukkitGameTaskScheduler;
import dev.tecte.chessWar.game.infrastructure.command.GameCommand;
import dev.tecte.chessWar.game.infrastructure.listener.GameVisibilityListener;
import dev.tecte.chessWar.game.infrastructure.persistence.YmlGameRepository;
import org.bukkit.event.Listener;

/**
 * 게임 도메인의 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 */
@SuppressWarnings("unused")
public class GameModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GameRepository.class).to(YmlGameRepository.class);
        bind(GameTaskScheduler.class).to(BukkitGameTaskScheduler.class);

        Multibinder.newSetBinder(binder(), PersistableState.class).addBinding().to(YmlGameRepository.class);
        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(GameCommand.class);
        Multibinder.newSetBinder(binder(), Listener.class).addBinding().to(GameVisibilityListener.class);
    }
}
