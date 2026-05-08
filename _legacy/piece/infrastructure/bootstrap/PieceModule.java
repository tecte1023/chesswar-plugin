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
import dev.tecte.chessWar.piece.infrastructure.listener.GameStopPieceCleanupListener;
import dev.tecte.chessWar.piece.infrastructure.listener.PieceInteractionListener;
import dev.tecte.chessWar.piece.infrastructure.listener.PieceVisibilityListener;
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
 * 기물 도메인의 의존성을 설정합니다.
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

        Multibinder<Listener> listenerBinder = Multibinder.newSetBinder(binder(), Listener.class);

        listenerBinder.addBinding().to(PieceInteractionListener.class);
        listenerBinder.addBinding().to(GameStopPieceCleanupListener.class);
        listenerBinder.addBinding().to(PieceVisibilityListener.class);
    }

    /**
     * 기물 배치 정보를 제공합니다.
     *
     * @param loader 기물 배치 로더
     * @return 기물 배치 정보
     */
    @NonNull
    @Provides
    @Singleton
    public PieceLayout provideInitialPieceLayout(@NonNull PieceLayoutLoader loader) {
        return loader.load();
    }

    /**
     * MythicMobs 매니저를 제공합니다.
     *
     * @return 매니저
     */
    @NonNull
    @Provides
    @Singleton
    public MobManager provideMobManager() {
        return MythicProvider.get().getMobManager();
    }

    /**
     * MythicMobs 실행기를 제공합니다.
     *
     * @param mobManager 매니저
     * @return 실행기
     * @throws IllegalStateException 매니저가 실행기 구현체가 아닐 경우
     */
    @NonNull
    @Provides
    @Singleton
    public MobExecutor provideMobExecutor(@NonNull MobManager mobManager) {
        if (mobManager instanceof MobExecutor executor) {
            return executor;
        }

        throw new IllegalStateException(
                "MobManager is not an instance of MobExecutor. Current implementation: %s"
                        .formatted(mobManager.getClass().getName())
        );
    }
}
