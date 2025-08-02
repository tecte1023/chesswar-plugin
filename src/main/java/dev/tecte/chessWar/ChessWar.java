package dev.tecte.chessWar;

import dev.tecte.chessWar.board.application.BoardRenderer;
import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.board.domain.repository.BoardConfigRepository;
import dev.tecte.chessWar.board.domain.service.BoardFactory;
import dev.tecte.chessWar.board.domain.service.OrientationCalculator;
import dev.tecte.chessWar.board.domain.service.SquareGridFactory;
import dev.tecte.chessWar.board.infrastructure.bukkit.BukkitBoardRenderer;
import dev.tecte.chessWar.board.infrastructure.config.YmlBoardConfigAdapter;
import dev.tecte.chessWar.board.interfaces.command.BoardTabCompleter;
import dev.tecte.chessWar.board.interfaces.command.CreateBoardCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ChessWar extends JavaPlugin {

    @Override
    public void onEnable() {
        BoardConfigRepository boardConfigRepository = new YmlBoardConfigAdapter(getConfig(), getLogger());
        OrientationCalculator orientationCalculator = new OrientationCalculator();
        SquareGridFactory squareGridFactory = new SquareGridFactory();

        BoardFactory boardFactory = new BoardFactory(boardConfigRepository, orientationCalculator, squareGridFactory);
        BoardRenderer boardRenderer = new BukkitBoardRenderer(boardConfigRepository);
        BoardService boardService = new BoardService(boardFactory, boardRenderer);

        Objects.requireNonNull(getCommand("board")).setExecutor(new CreateBoardCommand(boardService));
        Objects.requireNonNull(getCommand("board")).setTabCompleter(new BoardTabCompleter());
    }

    @Override
    public void onDisable() {

    }
}