package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.BorderConfig;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.board.domain.service.BoardCreationSpec;
import dev.tecte.chessWar.board.domain.service.BoardFactory;
import dev.tecte.chessWar.infrastructure.config.ConfigManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.logging.Level;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardService {
    private final BoardFactory boardFactory;
    private final BoardRenderer boardRenderer;
    private final BoardRepository boardRepository;
    private final ConfigManager configManager;
    private final ChessWar plugin;

    public void createBoard(@NonNull Player player) {
        Orientation orientation = Orientation.from(player.getFacing());
        BoardConfig boardConfig = configManager.getPluginConfig().boardConfig();
        SquareConfig squareConfig = boardConfig.squareConfig();
        BorderConfig innerBorderConfig = boardConfig.innerBorderConfig();
        BorderConfig frameConfig = boardConfig.frameConfig();

        int gridWidth = squareConfig.colCount() * squareConfig.width();
        int thickness = innerBorderConfig.thickness() + frameConfig.thickness();
        Vector offset = orientation.getLeft().clone().multiply(gridWidth / 2)
                .add(orientation.getForward().clone().multiply(thickness));
        Vector playerPosition = player.getLocation().toBlockLocation().toVector();
        BoardCreationSpec spec = BoardCreationSpec.builder()
                .gridAnchor(playerPosition.clone().add(offset))
                .orientation(orientation)
                .squareConfig(squareConfig)
                .innerBorderConfig(innerBorderConfig)
                .frameConfig(frameConfig)
                .build();
        Board board = boardFactory.createBoard(spec);
        World world = player.getWorld();

        boardRenderer.render(board, world);
        boardRepository.save(board);
        plugin.getLogger().log(Level.INFO, "Player ''{0}'' created a new chessboard at {1} in world ''{2}''",
                new Object[]{player.getName(), playerPosition, world.getName()});
    }
}
