package dev.tecte.chessWar.board.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.infrastructure.bukkit.BukkitBoardRenderer;
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

        // 여러 모듈에서 PersistableState와 ConfigUpdater의 구현체를 DI 컨테이너에 등록해야 하기 때문에 Multibinder를 사용
        // Multibinder를 사용하면, 각 모듈이 독립적으로 자신의 구현체를 Set<PersistableState>와 같은 컬렉션에 추가할 수 있음
        Multibinder<PersistableState> persistableStateBinder = Multibinder.newSetBinder(binder(), PersistableState.class);
        Multibinder<ConfigUpdater> configUpdaterBinder = Multibinder.newSetBinder(binder(), ConfigUpdater.class);

        persistableStateBinder.addBinding().to(YmlBoardRepository.class);
        configUpdaterBinder.addBinding().to(BoardConfigUpdater.class);
    }
}
