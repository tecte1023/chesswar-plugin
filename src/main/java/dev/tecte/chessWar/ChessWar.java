package dev.tecte.chessWar;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.tecte.chessWar.common.event.ChessWarStartedEvent;
import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.common.persistence.PersistableState;
import dev.tecte.chessWar.infrastructure.bootstrap.PluginModule;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.MythicMobsSetup;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 플러그인의 생명주기를 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@SuppressWarnings("unused")
public final class ChessWar extends JavaPlugin {
    @Inject
    private DomainEventDispatcher eventDispatcher;

    @Inject
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<PersistableState> persistableStates;

    @Inject
    private ExecutorService persistenceExecutor;

    @Inject
    private PaperCommandManager commandManager;

    /**
     * 도메인 엔진 및 인프라를 가동합니다.
     */
    @Override
    public void onEnable() {
        new MythicMobsSetup(this, getServer().getPluginManager()).run();

        Injector injector = Guice.createInjector(new PluginModule(this));

        injector.injectMembers(this);
        persistableStates.forEach(PersistableState::load);
        eventDispatcher.dispatch(ChessWarStartedEvent.of());
        log.atInfo().log("ChessWar plugin has been enabled!");
    }

    /**
     * 데이터 정합성을 보장하며 서비스를 종료합니다.
     */
    @Override
    public void onDisable() {
        // 최신 데이터 보존 및 덮어쓰기 방지를 위해 비동기 저장 중단
        if (persistenceExecutor != null) {
            persistenceExecutor.shutdownNow();
        }

        // 최종 메모리 상태를 저장소에 즉시 반영
        if (persistableStates != null) {
            persistableStates.forEach(PersistableState::flush);
        }

        if (commandManager != null) {
            commandManager.unregisterCommands();
        }

        HandlerList.unregisterAll(this);
        log.atInfo().log("ChessWar plugin has been disabled!");
    }
}
