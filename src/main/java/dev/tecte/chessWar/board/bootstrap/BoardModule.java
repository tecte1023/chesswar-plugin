package dev.tecte.chessWar.board.bootstrap;

import com.google.inject.AbstractModule;
import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.infrastructure.bukkit.BukkitBoardRenderer;

public class BoardModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BoardRenderer.class).to(BukkitBoardRenderer.class);
    }
}
