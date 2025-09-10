package dev.tecte.chessWar;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.tecte.chessWar.infrastructure.bootstrap.PluginModule;
import dev.tecte.chessWar.infrastructure.config.ConfigManager;
import dev.tecte.chessWar.infrastructure.persistence.PersistableState;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * ChessWar 플러그인의 메인 클래스입니다.
 * 플러그인의 생명주기(활성화/비활성화)를 관리하고, Guice를 사용한 의존성 주입을 설정합니다.
 */
@Slf4j(topic = "ChessWar")
@SuppressWarnings("unused")
public final class ChessWar extends JavaPlugin {
    @Inject
    private PaperCommandManager commandManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<PersistableState> persistableStates;

    /**
     * 플러그인이 활성화될 때 호출됩니다.
     * 기본 설정을 저장하고, 의존성 주입을 설정하며, 설정과 상태 데이터를 로드합니다.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();

        Injector injector = Guice.createInjector(new PluginModule(this));
        injector.injectMembers(this);

        if (configManager != null) {
            log.info("Loading configurations...");
            configManager.load();
        }

        if (persistableStates != null) {
            log.info("Loading all registered state data...");
            persistableStates.forEach(PersistableState::loadAll);
        }

        log.info("ChessWar plugin has been enabled!");
    }

    /**
     * 플러그인이 비활성화될 때 호출됩니다.
     * 캐시된 상태 데이터를 저장하고, 등록된 명령어를 해제합니다.
     */
    @Override
    public void onDisable() {
        if (persistableStates != null) {
            log.info("Saving all registered state data...");
            persistableStates.forEach(PersistableState::persistCache);
        }

        if (commandManager != null) {
            commandManager.unregisterCommands();
        }

        log.info("ChessWar plugin has been disabled!");
    }
}
