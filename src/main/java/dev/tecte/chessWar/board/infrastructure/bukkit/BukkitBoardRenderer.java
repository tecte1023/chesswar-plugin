package dev.tecte.chessWar.board.infrastructure.bukkit;

import dev.tecte.chessWar.board.application.BoardRenderer;
import dev.tecte.chessWar.board.domain.model.*;
import dev.tecte.chessWar.board.domain.repository.BoardConfigRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BoundingBox;

@RequiredArgsConstructor
public class BukkitBoardRenderer implements BoardRenderer {
    private final BoardConfigRepository boardConfigRepository;

    @Override
    public void render(Board board, World world) {
        BoardConfig config = boardConfigRepository.getBoardConfig();
        SquareConfig squareConfig = config.squareConfig();
        int rowCount = squareConfig.rowCount();
        int columnCount = squareConfig.columnCount();
        Square[][] squares = board.squares();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                BoundingBox boundingBox = squares[row][col].boundingBox();
                Material material = squares[row][col].color() == SquareColor.BLACK ? squareConfig.blackBlock() : squareConfig.whiteBlock();

                for (int x = (int) boundingBox.getMinX(); x < boundingBox.getMaxX(); x++) {
                    for (int z = (int) boundingBox.getMinZ(); z < boundingBox.getMaxZ(); z++) {
                        world.getBlockAt(x, (int) boundingBox.getMinY(), z).setType(material, false);
                    }
                }
            }
        }

        Border innerBorder = board.innerBorder();
        BoundingBox innerBoundingBox = innerBorder.boundingBox();
        int thickness = innerBorder.thickness();
        Material innerBorderMaterial = config.innerBorderConfig().block();
        BlockData innerBorderBlockDataX = Bukkit.createBlockData(innerBorderMaterial, data -> {
            if (data instanceof Orientable orientable) {
                orientable.setAxis(Axis.X);
            }
        });
        BlockData innerBorderBlockDataZ = Bukkit.createBlockData(innerBorderMaterial, data -> {
            if (data instanceof Orientable orientable) {
                orientable.setAxis(Axis.Z);
            }
        });

        for (int i = 0; i < thickness; i++) {
            for (int x = (int) innerBoundingBox.getMinX() + i; x < innerBoundingBox.getMaxX() - i; x++) {
                world.getBlockAt(x, (int) innerBoundingBox.getMinY(), (int) (innerBoundingBox.getMaxZ() - 1 - i)).setBlockData(innerBorderBlockDataX, false);
                world.getBlockAt(x, (int) innerBoundingBox.getMinY(), (int) (innerBoundingBox.getMinZ() + i)).setBlockData(innerBorderBlockDataX, false);
            }

            for (int z = (int) innerBoundingBox.getMinZ() + (i + 1); z < innerBoundingBox.getMaxZ() - (i + 1); z++) {
                world.getBlockAt((int) (innerBoundingBox.getMaxX() - 1 - i), (int) innerBoundingBox.getMinY(), z).setBlockData(innerBorderBlockDataZ, false);
                world.getBlockAt((int) (innerBoundingBox.getMinX() + i), (int) innerBoundingBox.getMinY(), z).setBlockData(innerBorderBlockDataZ, false);
            }
        }

        Border frame = board.frame();
        BoundingBox frameBoundingBox = frame.boundingBox();
        int frameThickness = frame.thickness();
        Material frameMaterial = config.frameConfig().block();
        BlockData frameBlockDataX = Bukkit.createBlockData(frameMaterial, data -> {
            if (data instanceof Orientable orientable) {
                orientable.setAxis(Axis.X);
            }
        });
        BlockData frameBlockDataZ = Bukkit.createBlockData(frameMaterial, data -> {
            if (data instanceof Orientable orientable) {
                orientable.setAxis(Axis.Z);
            }
        });

        for (int i = 0; i < frameThickness; i++) {
            for (int x = (int) frameBoundingBox.getMinX() + i; x < frameBoundingBox.getMaxX() - i; x++) {
                world.getBlockAt(x, (int) frameBoundingBox.getMinY(), (int) (frameBoundingBox.getMaxZ() - 1 - i)).setBlockData(frameBlockDataX, false);
                world.getBlockAt(x, (int) frameBoundingBox.getMinY(), (int) (frameBoundingBox.getMinZ() + i)).setBlockData(frameBlockDataX, false);
            }

            for (int z = (int) frameBoundingBox.getMinZ() + (i + 1); z < frameBoundingBox.getMaxZ() - (i + 1); z++) {
                world.getBlockAt((int) (frameBoundingBox.getMaxX() - 1 - i), (int) frameBoundingBox.getMinY(), z).setBlockData(frameBlockDataZ, false);
                world.getBlockAt((int) (frameBoundingBox.getMinX() + i), (int) frameBoundingBox.getMinY(), z).setBlockData(frameBlockDataZ, false);
            }
        }
    }
}