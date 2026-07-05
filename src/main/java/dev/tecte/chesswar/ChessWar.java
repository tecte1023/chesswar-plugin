package dev.tecte.chesswar;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j(topic = "ChessWar")
public final class ChessWar extends JavaPlugin {

    @Override
    public void onEnable() {
        log.info("ChessWar has been enabled!");
    }

    @Override
    public void onDisable() {
        log.info("ChessWar has been disabled!");
    }
}
