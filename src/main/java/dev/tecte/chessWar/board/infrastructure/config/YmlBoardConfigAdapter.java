package dev.tecte.chessWar.board.infrastructure.config;

import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.BorderConfig;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.board.domain.repository.BoardConfigRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class YmlBoardConfigAdapter implements BoardConfigRepository {
    private static final String PREFIX = "board.config.";

    private static final int DEFAULT_ROW_COUNT = 8;
    private static final int MIN_ROW_COUNT = 1;
    private static final int MAX_ROW_COUNT = 16;

    private static final int DEFAULT_COLUMN_COUNT = 8;
    private static final int MIN_COLUMN_COUNT = 1;
    private static final int MAX_COLUMN_COUNT = 16;

    private static final int DEFAULT_SQUARE_WIDTH = 3;
    private static final int MIN_SQUARE_WIDTH = 1;
    private static final int MAX_SQUARE_WIDTH = 7;

    private static final int DEFAULT_SQUARE_HEIGHT = 3;
    private static final int MIN_SQUARE_HEIGHT = 1;
    private static final int MAX_SQUARE_HEIGHT = 7;

    private static final int DEFAULT_INNER_BORDER_THICKNESS = 1;
    private static final int MIN_INNER_BORDER_THICKNESS = 0;
    private static final int MAX_INNER_BORDER_THICKNESS = 3;

    private static final int DEFAULT_FRAME_THICKNESS = 2;
    private static final int MIN_FRAME_THICKNESS = 0;
    private static final int MAX_FRAME_THICKNESS = 6;

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

    @Contract("-> new")
    private SquareConfig getSquareConfig() {
        final String squarePrefix = PREFIX + "square.";
        int rowCount = getValidatedInt(squarePrefix + "row_count", DEFAULT_ROW_COUNT, MIN_ROW_COUNT, MAX_ROW_COUNT, "row_count");
        int columnCount = getValidatedInt(squarePrefix + "column_count", DEFAULT_COLUMN_COUNT, MIN_COLUMN_COUNT, MAX_COLUMN_COUNT, "column_count");
        int width = getValidatedInt(squarePrefix + "width", DEFAULT_SQUARE_WIDTH, MIN_SQUARE_WIDTH, MAX_SQUARE_WIDTH, "width");
        int height = getValidatedInt(squarePrefix + "height", DEFAULT_SQUARE_HEIGHT, MIN_SQUARE_HEIGHT, MAX_SQUARE_HEIGHT, "height");
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

    @Contract("-> new")
    private @NotNull BorderConfig getInnerBorderConfig() {
        final String innerBorderPrefix = PREFIX + "inner_border.";
        int thickness = getValidatedInt(innerBorderPrefix + "thickness", DEFAULT_INNER_BORDER_THICKNESS, MIN_INNER_BORDER_THICKNESS, MAX_INNER_BORDER_THICKNESS, "inner border thickness");
        Material block = getMaterial(innerBorderPrefix + "block", Material.STRIPPED_OAK_WOOD);

        return new BorderConfig(thickness, block);
    }

    @Contract("-> new")
    private BorderConfig getFrameConfig() {
        final String framePrefix = PREFIX + "frame.";
        int thickness = getValidatedInt(framePrefix + "thickness", DEFAULT_FRAME_THICKNESS, MIN_FRAME_THICKNESS, MAX_FRAME_THICKNESS, "frame thickness");
        Material block = getMaterial(framePrefix + "block", Material.DARK_OAK_WOOD);

        return new BorderConfig(thickness, block);
    }

    private int getValidatedInt(String path, int defaultValue, int minValue, int maxValue, String description) {
        int value = configFile.getInt(path, defaultValue);

        if (value < minValue || value > maxValue) {
            logger.log(Level.WARNING, "Invalid {0} in config: ''{1}''. Using default: {2} (must be between {3} and {4})",
                    new Object[]{description, value, defaultValue, minValue, maxValue});

            return defaultValue;
        }

        return value;
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



