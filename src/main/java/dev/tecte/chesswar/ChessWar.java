package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.board.BoardCommand;
import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.piece.PieceCommand;
import dev.tecte.chesswar.piece.PieceView;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChessWar extends JavaPlugin {
    private BoardContext board;
    private SimulationEventBus eventBus;
    private PieceView pieceView;
    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        this.board = new BoardContext();
        this.eventBus = new SimulationEventBus();
        this.pieceView = PieceView.create(this.eventBus, this.board);

        this.commandManager = new PaperCommandManager(this);
        this.commandManager.registerCommand(new BoardCommand(this.board));
        this.commandManager.registerCommand(new PieceCommand(this.eventBus));
        
        getLogger().info("ChessWar 시스템이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChessWar 시스템이 비활성화되었습니다.");
    }
}

