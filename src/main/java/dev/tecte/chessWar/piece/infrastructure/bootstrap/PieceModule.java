package dev.tecte.chessWar.piece.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.piece.application.port.PieceIdResolver;
import dev.tecte.chessWar.piece.application.port.PieceInfoRenderer;
import dev.tecte.chessWar.piece.application.port.PieceLayoutLoader;
import dev.tecte.chessWar.piece.application.port.PieceSpawner;
import dev.tecte.chessWar.piece.application.port.PieceStatProvider;
import dev.tecte.chessWar.piece.domain.model.PieceLayout;
import dev.tecte.chessWar.piece.infrastructure.bukkit.BukkitPieceInfoRenderer;
import dev.tecte.chessWar.piece.infrastructure.command.PieceCommand;
import dev.tecte.chessWar.piece.infrastructure.listener.PieceInteractionListener;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.MythicMobsPieceIdResolver;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.MythicMobsPieceLayoutLoader;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.MythicMobsPieceSpawner;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.MythicMobsPieceStatProvider;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.core.mobs.MobExecutor;
import lombok.NonNull;
import org.bukkit.event.Listener;

/**
 * 기물 도메인의 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 */
@SuppressWarnings("unused")
public class PieceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PieceLayoutLoader.class).to(MythicMobsPieceLayoutLoader.class);
        bind(PieceSpawner.class).to(MythicMobsPieceSpawner.class);
        bind(PieceInfoRenderer.class).to(BukkitPieceInfoRenderer.class);
        bind(PieceStatProvider.class).to(MythicMobsPieceStatProvider.class);
        bind(PieceIdResolver.class).to(MythicMobsPieceIdResolver.class);

        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(PieceCommand.class);
        Multibinder.newSetBinder(binder(), Listener.class).addBinding().to(PieceInteractionListener.class);
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
     * MythicMobs의 구현체인 {@link MobExecutor}를 Guice 컨테이너에 제공합니다.
     * 고급 기능을 사용하기 위해 제공됩니다.
     *
     * @param mobManager {@link MobManager} 인스턴스
     * @return {@link MobExecutor} 인스턴스
     * @throws IllegalStateException MobManager가 MobExecutor의 인스턴스가 아닐 경우
     */
    @NonNull
    @Provides
    @Singleton
    public MobExecutor provideMobExecutor(@NonNull MobManager mobManager) {
        if (mobManager instanceof MobExecutor executor) {
            return executor;
        }

        throw new IllegalStateException(
                "MobManager is not an instance of MobExecutor. Current implementation: " + mobManager.getClass().getName()
        );
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
