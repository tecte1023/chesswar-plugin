package dev.tecte.chesswar;

import co.aikar.commands.PaperCommandManager;
import dev.tecte.chesswar.board.BoardCommand;
import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.piece.PieceCommand;
import dev.tecte.chesswar.piece.PieceLifecycle;
import dev.tecte.chesswar.piece.PieceView;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChessWar extends JavaPlugin {
    private PieceView pieceView;

    @Override
    public void onEnable() {
        var board = new BoardContext();
        var eventBus = new SimulationEventBus();
        var pieceLifecycle = new PieceLifecycle(eventBus);
        pieceView = PieceView.create(board, eventBus);

        var commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new BoardCommand(board));
        commandManager.registerCommand(new PieceCommand(board, pieceLifecycle));
    }

    @Override
    public void onDisable() {
        if (pieceView != null) {
            pieceView.cleanup();
        }
    }
}

