package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.BorderConfig;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.board.domain.service.BoardCreationSpec;
import dev.tecte.chessWar.board.domain.service.BoardFactory;
import dev.tecte.chessWar.config.ConfigManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BoardService {
    private final BoardFactory boardFactory;
    private final BoardRenderer boardRenderer;
    private final ConfigManager configManager;

    public void createBoard(@NotNull Player player) {
        Orientation orientation = Orientation.from(player.getFacing());
        BoardConfig boardConfig = configManager.getPluginConfig().boardConfig();
        SquareConfig squareConfig = boardConfig.squareConfig();
        BorderConfig innerBorderConfig = boardConfig.innerBorderConfig();
        BorderConfig frameConfig = boardConfig.frameConfig();

        int gridWidth = squareConfig.columnCount() * squareConfig.width();
        int thickness = innerBorderConfig.thickness() + frameConfig.thickness();
        Vector offset = orientation.getLeft().clone().multiply(gridWidth / 2)
                .add(orientation.getForward().clone().multiply(thickness));
        Vector gridAnchor = player.getLocation().toBlockLocation().toVector().add(offset);

        BoardCreationSpec spec = BoardCreationSpec.builder()
                .gridAnchor(gridAnchor)
                .orientation(orientation)
                .squareConfig(squareConfig)
                .innerBorderConfig(innerBorderConfig)
                .frameConfig(frameConfig)
                .build();
        Board board = boardFactory.createBoard(spec);

        boardRenderer.render(board, player.getWorld());
    }
}
