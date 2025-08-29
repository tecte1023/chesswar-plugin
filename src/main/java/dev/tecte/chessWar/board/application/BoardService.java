package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.config.ConfigManager;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.domain.service.BoardCreationSpec;
import dev.tecte.chessWar.board.domain.service.BoardFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class BoardService {
    private final BoardFactory boardFactory;
    private final BoardRenderer boardRenderer;
    private final ConfigManager configManager;

    public void createBoard(@NotNull Player player) {
        BlockVector playerPosition = player.getLocation().toVector().toBlockVector();
        Orientation orientation = Orientation.from(player.getFacing());
        BoardConfig boardConfig = configManager.getPluginConfig().boardConfig();
        SquareConfig squareConfig = boardConfig.squareConfig();

        int gridWidth = squareConfig.columnCount() * squareConfig.width();
        Vector offset = orientation.getLeft().clone().multiply(gridWidth / 2)
                .add(orientation.getForward());
        BlockVector gridAnchor = playerPosition.clone().add(offset).toBlockVector();

        BoardCreationSpec spec = BoardCreationSpec.builder()
                .gridAnchor(gridAnchor)
                .orientation(orientation)
                .squareConfig(squareConfig)
                .innerBorderConfig(boardConfig.innerBorderConfig())
                .frameConfig(boardConfig.frameConfig())
                .build();
        Board board = boardFactory.createBoard(spec);

        boardRenderer.render(board, player.getWorld());
    }
}
