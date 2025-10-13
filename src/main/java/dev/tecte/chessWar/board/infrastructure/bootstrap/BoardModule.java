package dev.tecte.chessWar.board.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.infrastructure.bukkit.BukkitBoardRenderer;
import dev.tecte.chessWar.board.infrastructure.command.BoardCommand;
import dev.tecte.chessWar.board.infrastructure.persistence.YmlBoardRepository;
import dev.tecte.chessWar.infrastructure.config.ConfigUpdater;
import dev.tecte.chessWar.infrastructure.persistence.PersistableState;

/**
 * 체스판 모듈의 의존성 주입(DI) 설정을 담당하는 Guice 모듈입니다.
 * 체스판 기능에 필요한 인터페이스와 구현체를 바인딩합니다.
 */
public class BoardModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BoardRepository.class).to(YmlBoardRepository.class);
        bind(BoardRenderer.class).to(BukkitBoardRenderer.class);

        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(BoardCommand.class);
        Multibinder.newSetBinder(binder(), PersistableState.class).addBinding().to(YmlBoardRepository.class);
        Multibinder.newSetBinder(binder(), ConfigUpdater.class).addBinding().to(BoardConfigUpdater.class);
    }
}
