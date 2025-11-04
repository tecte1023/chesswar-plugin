package dev.tecte.chessWar;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.tecte.chessWar.common.persistence.PersistableState;
import dev.tecte.chessWar.game.infrastructure.mythicmobs.MythicMobsSetup;
import dev.tecte.chessWar.infrastructure.bootstrap.PluginModule;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceInitializationException;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.HandlerList;
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
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<PersistableState> persistableStates;

    /**
     * 플러그인이 활성화될 때 호출됩니다.
     * 기본 설정을 저장하고, 의존성 주입을 설정하며, 설정과 상태 데이터를 로드합니다.
     */
    @Override
    public void onEnable() {
        new MythicMobsSetup(getServer().getPluginManager(), this).run();

        Injector injector = Guice.createInjector(new PluginModule(this));
        injector.injectMembers(this);

        if (persistableStates != null) {
            log.info("Loading all registered state data...");

            try {
                persistableStates.forEach(PersistableState::load);
            } catch (PersistenceInitializationException e) {
                log.error("Failed to initialize persistence layer. The plugin will be disabled.", e);
                getServer().getPluginManager().disablePlugin(this);

                return;
            }
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

        HandlerList.unregisterAll(this);
        log.info("ChessWar plugin has been disabled!");
    }
}
