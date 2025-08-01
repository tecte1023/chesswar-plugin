package dev.tecte.chessWar.board.infrastructure.config;

import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.BorderConfig;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.board.domain.repository.BoardConfigRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class YmlBoardConfigAdapter implements BoardConfigRepository {
    private static final String PREFIX = "board.config.";
    private final FileConfiguration configFile;
    private final Logger logger;

    @Override
    public BoardConfig getBoardConfig() {
        SquareConfig squareConfig = getSquareConfig();
        BorderConfig innerBorderConfig = getInnerBorderConfig();
        BorderConfig frameConfig = getFrameConfig();

        return BoardConfig.builder()
                .squareConfig(squareConfig)
                .innerBorderConfig(innerBorderConfig)
                .frameConfig(frameConfig)
                .build();
    }

    private SquareConfig getSquareConfig() {
        final String squarePrefix = PREFIX + "square.";
        int rowCount = configFile.getInt(squarePrefix + "row_count", 8);
        int columnCount = configFile.getInt(squarePrefix + "column_count", 8);
        int width = configFile.getInt(squarePrefix + "width", 3);
        int height = configFile.getInt(squarePrefix + "height", 3);
        Material blackBlock = getMaterial(squarePrefix + "black_block", Material.STRIPPED_DARK_OAK_WOOD);
        Material whiteBlock = getMaterial(squarePrefix + "white_block", Material.STRIPPED_PALE_OAK_WOOD);

        return SquareConfig.builder()
                .rowCount(rowCount)
                .columnCount(columnCount)
                .width(width)
                .height(height)
                .blackBlock(blackBlock)
                .whiteBlock(whiteBlock)
                .build();
    }

    private BorderConfig getInnerBorderConfig() {
        final String innerBorderPrefix = PREFIX + "inner_border.";
        int thickness = configFile.getInt(innerBorderPrefix + "thickness", 1);
        Material block = getMaterial(innerBorderPrefix + "block", Material.STRIPPED_OAK_WOOD);

        return new BorderConfig(thickness, block);
    }

    private BorderConfig getFrameConfig() {
        final String framePrefix = PREFIX + "frame.";
        int thickness = configFile.getInt(framePrefix + "thickness", 2);
        Material block = getMaterial(framePrefix + "block", Material.DARK_OAK_WOOD);

        return new BorderConfig(thickness, block);
    }

    private Material getMaterial(String path, Material defaultValue) {
        String materialName = configFile.getString(path);

        if (materialName == null || materialName.isBlank()) {
            logger.log(Level.WARNING, "Material not found at ''{0}''. Using default: {1}",
                    new Object[]{path, defaultValue.name()});

            return defaultValue;
        }

        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid material name in config: ''{0}'' at path: ''{1}''. Using default: {2}",
                    new Object[]{materialName, path, defaultValue.name()});

            return defaultValue;
        }
    }
}



