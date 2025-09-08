package dev.tecte.chessWar;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.tecte.chessWar.infrastructure.bootstrap.PluginModule;
import dev.tecte.chessWar.infrastructure.config.ConfigManager;
import dev.tecte.chessWar.infrastructure.persistence.PersistableState;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

@SuppressWarnings("unused")
public final class ChessWar extends JavaPlugin {
    @Inject
    private PaperCommandManager commandManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<PersistableState> persistableStates;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        Injector injector = Guice.createInjector(new PluginModule(this));
        injector.injectMembers(this);

        if (configManager != null) {
            getLogger().info("Loading configurations...");
            configManager.load();
        }

        if (persistableStates != null) {
            getLogger().info("Loading all registered state data...");
            persistableStates.forEach(PersistableState::loadAll);
        }

        getLogger().info("ChessWar plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (persistableStates != null) {
            getLogger().info("Saving all registered state data...");
            persistableStates.forEach(PersistableState::persistCache);
        }

        if (commandManager != null) {
            commandManager.unregisterCommands();
        }

        getLogger().info("ChessWar plugin has been disabled!");
    }
}
