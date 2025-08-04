package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.*;
import dev.tecte.chessWar.board.domain.repository.BoardConfigRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BoardFactory {
    private final BoardConfigRepository boardConfigRepository;

    @Contract("_ -> new")
    public Board createBoard(@NotNull Player player) {
        BoardConfig boardConfig = boardConfigRepository.getBoardConfig();
        Orientation orientation = Orientation.from(player.getFacing());

        SquareGrid squares = SquareGrid.from(boardConfig.squareConfig(), orientation, createGridBoundingBox(player, boardConfig, orientation));
        Border innerBorder = Border.from(squares.getBoundingBox(), boardConfig.innerBorderConfig().thickness());
        Border frame = Border.from(innerBorder.boundingBox(), boardConfig.frameConfig().thickness());

        return Board.builder()
                .squares(squares)
                .innerBorder(innerBorder)
                .frame(frame)
                .build();
    }

    @Contract("_, _, _ -> new")
    private @NotNull BoundingBox createGridBoundingBox(@NotNull Player player, @NotNull BoardConfig boardConfig, @NotNull Orientation orientation) {
        SquareConfig squareConfig = boardConfig.squareConfig();
        int innerBorderThickness = boardConfig.innerBorderConfig().thickness();
        int frameThickness = boardConfig.frameConfig().thickness();
        int width = squareConfig.width();
        int height = squareConfig.height();
        int columnCount = squareConfig.columnCount();

        return BoundingBox.of(player.getLocation().getBlock())
                .shift(orientation.forward().clone().multiply(innerBorderThickness + frameThickness + 1))
                .shift(orientation.left().clone().multiply(width * columnCount / 2))
                .expand(orientation.right(), width - 1)
                .expand(orientation.forward(), height - 1);
    }
}
