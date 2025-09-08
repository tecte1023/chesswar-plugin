package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.BorderConfig;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.BLACK_BLOCK_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.COL_COUNT_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.FRAME_BLOCK_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.FRAME_THICKNESS_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.HEIGHT_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.INNER_BORDER_BLOCK_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.INNER_BORDER_THICKNESS_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.ROW_COUNT_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.WHITE_BLOCK_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.WIDTH_PATH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_COL_COUNT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_FRAME_BLOCK;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_FRAME_THICKNESS;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_INNER_BORDER_BLOCK;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_INNER_BORDER_THICKNESS;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_ROW_COUNT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_SQUARE_BLACK_BLOCK;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_SQUARE_HEIGHT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_SQUARE_WHITE_BLOCK;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.DEFAULT_SQUARE_WIDTH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MAX_COL_COUNT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MAX_FRAME_THICKNESS;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MAX_INNER_BORDER_THICKNESS;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MAX_ROW_COUNT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MAX_SQUARE_HEIGHT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MAX_SQUARE_WIDTH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MIN_COL_COUNT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MIN_FRAME_THICKNESS;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MIN_INNER_BORDER_THICKNESS;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MIN_ROW_COUNT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MIN_SQUARE_HEIGHT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Defaults.MIN_SQUARE_WIDTH;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class YmlBoardConfigRepository {
    private final FileConfiguration defaultConfig;
    private final YmlFileManager userDataConfigManager;
    private final Logger logger;

    @NonNull
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

    public void save(@NonNull BoardConfig boardConfig) {
        saveSquareConfig(boardConfig.squareConfig());
        saveInnerBorderConfig(boardConfig.innerBorderConfig());
        saveFrameConfig(boardConfig.frameConfig());
        userDataConfigManager.save();
    }

    @NonNull
    private SquareConfig getSquareConfig() {
        int rowCount = getValidatedInt(ROW_COUNT_PATH, DEFAULT_ROW_COUNT, MIN_ROW_COUNT, MAX_ROW_COUNT);
        int colCount = getValidatedInt(COL_COUNT_PATH, DEFAULT_COL_COUNT, MIN_COL_COUNT, MAX_COL_COUNT);
        int width = getValidatedInt(WIDTH_PATH, DEFAULT_SQUARE_WIDTH, MIN_SQUARE_WIDTH, MAX_SQUARE_WIDTH);
        int height = getValidatedInt(HEIGHT_PATH, DEFAULT_SQUARE_HEIGHT, MIN_SQUARE_HEIGHT, MAX_SQUARE_HEIGHT);
        Material blackBlock = getValidatedMaterial(BLACK_BLOCK_PATH, DEFAULT_SQUARE_BLACK_BLOCK);
        Material whiteBlock = getValidatedMaterial(WHITE_BLOCK_PATH, DEFAULT_SQUARE_WHITE_BLOCK);

        return SquareConfig.builder()
                .rowCount(rowCount)
                .colCount(colCount)
                .width(width)
                .height(height)
                .blackBlock(blackBlock)
                .whiteBlock(whiteBlock)
                .build();
    }

    @NonNull
    private BorderConfig getInnerBorderConfig() {
        int thickness = getValidatedInt(INNER_BORDER_THICKNESS_PATH, DEFAULT_INNER_BORDER_THICKNESS, MIN_INNER_BORDER_THICKNESS, MAX_INNER_BORDER_THICKNESS);
        Material block = getValidatedMaterial(INNER_BORDER_BLOCK_PATH, DEFAULT_INNER_BORDER_BLOCK);

        return new BorderConfig(thickness, block);
    }

    @NonNull
    private BorderConfig getFrameConfig() {
        int thickness = getValidatedInt(FRAME_THICKNESS_PATH, DEFAULT_FRAME_THICKNESS, MIN_FRAME_THICKNESS, MAX_FRAME_THICKNESS);
        Material block = getValidatedMaterial(FRAME_BLOCK_PATH, DEFAULT_FRAME_BLOCK);

        return new BorderConfig(thickness, block);
    }

    public int getValidatedInt(
            @NonNull String path,
            int fallback,
            int minValue,
            int maxValue
    ) {
        return findValidInt(userDataConfigManager.getConfig(), path, minValue, maxValue)
                .or(() -> findValidInt(defaultConfig, path, minValue, maxValue))
                .orElse(fallback);
    }

    @NonNull
    private Optional<Integer> findValidInt(
            @NonNull FileConfiguration config,
            @NonNull String path,
            int minValue,
            int maxValue
    ) {
        if (!config.contains(path)) {
            return Optional.empty();
        }

        int value = config.getInt(path);

        if (value >= minValue && value <= maxValue) {
            return Optional.of(value);
        }

        logger.log(Level.WARNING, "Invalid value in ''{0}'' at ''{1}'': {2}",
                new Object[]{config.getName(), path, value});

        return Optional.empty();
    }

    @NonNull
    private Material getValidatedMaterial(@NonNull String path, @NonNull Material fallback) {
        return findValidMaterial(userDataConfigManager.getConfig(), path)
                .or(() -> findValidMaterial(defaultConfig, path))
                .orElse(fallback);
    }

    @NonNull
    private Optional<Material> findValidMaterial(@NonNull FileConfiguration config, @NonNull String path) {
        if (!config.contains(path)) {
            return Optional.empty();
        }

        String materialName = config.getString(path);

        if (materialName == null || materialName.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Material.valueOf(materialName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid material name in ''{0}'' at ''{1}'': ''{2}''",
                    new Object[]{config.getName(), path, materialName});

            return Optional.empty();
        }
    }

    private void saveSquareConfig(@NonNull SquareConfig squareConfig) {
        userDataConfigManager.set(ROW_COUNT_PATH, squareConfig.rowCount());
        userDataConfigManager.set(COL_COUNT_PATH, squareConfig.colCount());
        userDataConfigManager.set(WIDTH_PATH, squareConfig.width());
        userDataConfigManager.set(HEIGHT_PATH, squareConfig.height());
        userDataConfigManager.set(BLACK_BLOCK_PATH, squareConfig.blackBlock().name());
        userDataConfigManager.set(WHITE_BLOCK_PATH, squareConfig.whiteBlock().name());
    }

    private void saveInnerBorderConfig(@NonNull BorderConfig innerBorderConfig) {
        userDataConfigManager.set(INNER_BORDER_THICKNESS_PATH, innerBorderConfig.thickness());
        userDataConfigManager.set(INNER_BORDER_BLOCK_PATH, innerBorderConfig.block().name());
    }

    private void saveFrameConfig(@NonNull BorderConfig frameConfig) {
        userDataConfigManager.set(FRAME_THICKNESS_PATH, frameConfig.thickness());
        userDataConfigManager.set(FRAME_BLOCK_PATH, frameConfig.block().name());
    }
}
