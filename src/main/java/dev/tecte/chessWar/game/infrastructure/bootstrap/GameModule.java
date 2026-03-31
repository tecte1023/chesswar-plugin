package dev.tecte.chessWar.game.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.common.persistence.PersistableState;
import dev.tecte.chessWar.game.application.GameAnnouncer;
import dev.tecte.chessWar.game.application.GameFlowCoordinator;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.game.infrastructure.bukkit.BukkitGameTaskManager;
import dev.tecte.chessWar.game.infrastructure.command.GameCommand;
import dev.tecte.chessWar.game.infrastructure.listener.GameSelectionPhaseInitiator;
import dev.tecte.chessWar.game.infrastructure.listener.GameSelectionStartAnnouncer;
import dev.tecte.chessWar.game.infrastructure.listener.GameStartBattlefieldSetupInitiator;
import dev.tecte.chessWar.game.infrastructure.listener.GameStopAnnounceListener;
import dev.tecte.chessWar.game.infrastructure.listener.GameStopTaskCleanupListener;
import dev.tecte.chessWar.game.infrastructure.listener.PlayerJoinInitiator;
import dev.tecte.chessWar.game.infrastructure.listener.PieceInspectionInitiator;
import dev.tecte.chessWar.game.infrastructure.listener.PieceSelectionAnnounceListener;
import dev.tecte.chessWar.game.infrastructure.persistence.YmlGameRepository;
import org.bukkit.event.Listener;

/**
 * 게임 도메인의 의존성을 설정합니다.
 */
@SuppressWarnings("unused")
public class GameModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GameRepository.class).to(YmlGameRepository.class);
        bind(GameTaskManager.class).to(BukkitGameTaskManager.class);
        bind(GameFlowCoordinator.class).asEagerSingleton();
        bind(GameAnnouncer.class).asEagerSingleton();

        Multibinder.newSetBinder(binder(), PersistableState.class).addBinding().to(YmlGameRepository.class);
        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(GameCommand.class);

        Multibinder<Listener> listenerBinder = Multibinder.newSetBinder(binder(), Listener.class);

        listenerBinder.addBinding().to(PlayerJoinInitiator.class);
        listenerBinder.addBinding().to(GameStartBattlefieldSetupInitiator.class);
        listenerBinder.addBinding().to(GameSelectionPhaseInitiator.class);
        listenerBinder.addBinding().to(PieceInspectionInitiator.class);
        listenerBinder.addBinding().to(GameSelectionStartAnnouncer.class);
        listenerBinder.addBinding().to(PieceSelectionAnnounceListener.class);
        listenerBinder.addBinding().to(GameStopAnnounceListener.class);
        listenerBinder.addBinding().to(GameStopTaskCleanupListener.class);
    }
}
