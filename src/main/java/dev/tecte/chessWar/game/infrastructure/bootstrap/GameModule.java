package dev.tecte.chessWar.game.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.game.application.port.PieceLayoutLoader;
import dev.tecte.chessWar.game.domain.model.PieceLayout;
import dev.tecte.chessWar.game.infrastructure.command.GameCommand;
import dev.tecte.chessWar.game.infrastructure.mythicmobs.MythicMobsPieceLayoutLoader;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import lombok.NonNull;

/**
 * 게임 도메인의 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 */
@SuppressWarnings("unused")
public class GameModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PieceLayoutLoader.class).to(MythicMobsPieceLayoutLoader.class);

        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(GameCommand.class);
    }

    /**
     * MythicMobs의 {@link MobManager}를 Guice 컨테이너에 제공합니다.
     *
     * @return {@link MobManager}의 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    public MobManager provideMobManager() {
        return MythicProvider.get().getMobManager();
    }

    /**
     * 초기 체스 말 배치 정보를 담고 있는 {@link PieceLayout} 객체를 Guice 컨테이너에 제공합니다.
     * 이 객체는 애플리케이션 시작 시 한 번만 로드됩니다.
     *
     * @param loader 체스 말 배치 정보를 로드하는 데 사용될 로더
     * @return 로드된 {@link PieceLayout} 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    public PieceLayout provideInitialPieceLayout(@NonNull PieceLayoutLoader loader) {
        return loader.load();
    }
}
