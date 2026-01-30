package dev.tecte.chessWar;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.tecte.chessWar.common.persistence.PersistableState;
import dev.tecte.chessWar.infrastructure.bootstrap.PluginModule;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.MythicMobsSetup;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * ChessWar 플러그인의 메인 클래스입니다.
 * 플러그인의 생명주기(활성화/비활성화)를 관리하고, Guice를 사용한 의존성 주입을 설정합니다.
 */
@Slf4j(topic = "ChessWar")
@SuppressWarnings("unused")
public final class ChessWar extends JavaPlugin {
    @Inject
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<PersistableState> persistableStates;

    @Inject
    private ExecutorService persistenceExecutor;

    @Inject
    private PaperCommandManager commandManager;

    /**
     * 플러그인이 활성화될 때 호출됩니다.
     * 기본 설정을 저장하고, 의존성 주입을 설정하며, 설정과 상태 데이터를 로드합니다.
     */
    @Override
    public void onEnable() {
        new MythicMobsSetup(getServer().getPluginManager(), this).run();

        Injector injector = Guice.createInjector(new PluginModule(this));

        injector.injectMembers(this);
        persistableStates.forEach(PersistableState::load);
        log.info("ChessWar plugin has been enabled!");
    }

    /**
     * 플러그인이 비활성화될 때 호출됩니다.
     * 캐시된 상태 데이터를 저장하고, 등록된 리소스를 안전하게 해제합니다.
     */
    @Override
    public void onDisable() {
        // 비동기 작업 취소 (메모리 상태가 더 최신이므로 persistCache()로 덮어씀)
        if (persistenceExecutor != null) {
            persistenceExecutor.shutdownNow();
        }

        if (persistableStates != null) {
            persistableStates.forEach(PersistableState::flush);
        }

        if (commandManager != null) {
            commandManager.unregisterCommands();
        }

        HandlerList.unregisterAll(this);
        log.info("ChessWar plugin has been disabled!");
    }
}
