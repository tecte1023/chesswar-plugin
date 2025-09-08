package dev.tecte.chessWar.board.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.infrastructure.bukkit.BukkitBoardRenderer;
import dev.tecte.chessWar.board.infrastructure.persistence.YmlBoardRepository;
import dev.tecte.chessWar.infrastructure.config.ConfigUpdater;
import dev.tecte.chessWar.infrastructure.persistence.PersistableState;

public class BoardModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BoardRepository.class).to(YmlBoardRepository.class);
        bind(BoardRenderer.class).to(BukkitBoardRenderer.class);

        Multibinder<PersistableState> persistableStateBinder = Multibinder.newSetBinder(binder(), PersistableState.class);
        Multibinder<ConfigUpdater> configUpdaterBinder = Multibinder.newSetBinder(binder(), ConfigUpdater.class);

        persistableStateBinder.addBinding().to(YmlBoardRepository.class);
        configUpdaterBinder.addBinding().to(BoardConfigUpdater.class);
    }
}
