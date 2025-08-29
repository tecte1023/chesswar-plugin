package dev.tecte.chessWar;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.tecte.chessWar.bootstrap.PluginModule;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("FieldMayBeFinal")
public final class ChessWar extends JavaPlugin {
    private PaperCommandManager commandManager = null;
//    @Inject
//    private TeamPersistenceScheduler teamPersistenceScheduler = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Injector injector = Guice.createInjector(new PluginModule(this));

        commandManager = injector.getInstance(PaperCommandManager.class);
        getLogger().info("ChessWar plugin has been enabled!");
    }

    @Override
    public void onDisable() {
//        if (teamPersistenceScheduler != null) {
//            teamPersistenceScheduler.shutdown();
//        }

        if (commandManager != null) {
            commandManager.unregisterCommands();
        }

        getLogger().info("ChessWar plugin has been disabled!");
    }
}
