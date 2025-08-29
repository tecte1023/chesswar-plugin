package dev.tecte.chessWar.board.infrastructure.bukkit;

import dev.tecte.chessWar.config.ConfigManager;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.Square;
import dev.tecte.chessWar.board.domain.model.SquareColor;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.board.application.port.BoardRenderer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class BukkitBoardRenderer implements BoardRenderer {
    private final ConfigManager configManager;

    @Override
    public void render(@NotNull Board board, @NotNull World world) {
        BoardConfig config = configManager.getPluginConfig().boardConfig();

        renderSquares(board, world, config.squareConfig());
        renderBorder(board.innerBorder(), world, config.innerBorderConfig().block());
        renderBorder(board.frame(), world, config.frameConfig().block());
    }

    private void renderSquares(@NotNull Board board, @NotNull World world, @NotNull SquareConfig squareConfig) {
        int rowCount = squareConfig.rowCount();
        int columnCount = squareConfig.columnCount();
        SquareGrid squareGrid = board.squareGrid();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                Square square = squareGrid.getSquareAt(row, col);
                BoundingBox boundingBox = square.boundingBox();
                Material material = square.color() == SquareColor.BLACK ? squareConfig.blackBlock() : squareConfig.whiteBlock();

                for (int x = (int) boundingBox.getMinX(); x < boundingBox.getMaxX(); x++) {
                    for (int z = (int) boundingBox.getMinZ(); z < boundingBox.getMaxZ(); z++) {
                        world.getBlockAt(x, (int) boundingBox.getMinY(), z).setType(material, false);
                    }
                }
            }
        }
    }

    private void renderBorder(@NotNull Border border, @NotNull World world, @NotNull Material material) {
        BlockData blockDataX = createOrientableBlockData(material, Axis.X);
        BlockData blockDataZ = createOrientableBlockData(material, Axis.Z);

        BoundingBox boundingBox = border.boundingBox();
        int thickness = border.thickness();

        int minY = (int) boundingBox.getMinY();
        int minX = (int) boundingBox.getMinX();
        int minZ = (int) boundingBox.getMinZ();
        int maxX = (int) boundingBox.getMaxX();
        int maxZ = (int) boundingBox.getMaxZ();

        for (int i = 0; i < thickness; i++) {
            for (int x = minX + i; x < maxX - i; x++) {
                world.getBlockAt(x, minY, maxZ - 1 - i).setBlockData(blockDataX, false);
                world.getBlockAt(x, minY, minZ + i).setBlockData(blockDataX, false);
            }

            for (int z = minZ + i + 1; z < maxZ - i - 1; z++) {
                world.getBlockAt(maxX - 1 - i, minY, z).setBlockData(blockDataZ, false);
                world.getBlockAt(minX + i, minY, z).setBlockData(blockDataZ, false);
            }
        }
    }

    @NotNull
    private BlockData createOrientableBlockData(@NotNull Material material, @NotNull Axis axis) {
        return Bukkit.createBlockData(material, data -> {
            if (data instanceof Orientable orientable) {
                orientable.setAxis(axis);
            }
        });
    }
}
