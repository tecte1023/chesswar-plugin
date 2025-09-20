package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.FrameConfig;
import dev.tecte.chessWar.board.domain.model.InnerBorderConfig;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.infrastructure.config.ConfigResolver;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import static dev.tecte.chessWar.board.infrastructure.persistence.BoardPersistenceConstants.ConfigPaths;

/**
 * YML 파일에서 체스판의 정적 설정({@link BoardConfig})을 로드하고 저장하는 리포지토리 클래스입니다.
 * 사용자 설정 > 초기 설정 > 플러그인 기본 설정 순으로 우선순위를 고려하여 설정 값을 결정합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class YmlBoardConfigRepository {
    private final ConfigResolver configResolver;
    private final YmlFileManager userDataConfigManager;

    /**
     * 체스판 설정을 불러옵니다.
     *
     * @return 로드된 체스판 설정 객체
     */
    @NonNull
    public BoardConfig getBoardConfig() {
        return new BoardConfig(getSquareConfig(), getInnerBorderConfig(), getFrameConfig());
    }

    /**
     * 주어진 체스판 설정을 사용자 설정 파일에 저장합니다.
     *
     * @param boardConfig 저장할 체스판 설정
     */
    public void save(@NonNull BoardConfig boardConfig) {
        saveSquareConfig(boardConfig.squareConfig());
        saveInnerBorderConfig(boardConfig.innerBorderConfig());
        saveFrameConfig(boardConfig.frameConfig());
        userDataConfigManager.save();
    }

    @NonNull
    private SquareConfig getSquareConfig() {
        int rowCount = configResolver.resolveInt(ConfigPaths.ROW_COUNT_PATH, SquareConfig.DEFAULT_ROW_COUNT, SquareConfig::isValidRowCount);
        int colCount = configResolver.resolveInt(ConfigPaths.COL_COUNT_PATH, SquareConfig.DEFAULT_COL_COUNT, SquareConfig::isValidColCount);
        int width = configResolver.resolveInt(ConfigPaths.WIDTH_PATH, SquareConfig.DEFAULT_WIDTH, SquareConfig::isValidWidth);
        int height = configResolver.resolveInt(ConfigPaths.HEIGHT_PATH, SquareConfig.DEFAULT_HEIGHT, SquareConfig::isValidHeight);
        Material blackBlock = resolveMaterial(ConfigPaths.BLACK_BLOCK_PATH, SquareConfig.DEFAULT_BLACK_BLOCK);
        Material whiteBlock = resolveMaterial(ConfigPaths.WHITE_BLOCK_PATH, SquareConfig.DEFAULT_WHITE_BLOCK);

        return new SquareConfig(rowCount, colCount, width, height, blackBlock, whiteBlock);
    }

    @NonNull
    private InnerBorderConfig getInnerBorderConfig() {
        int thickness = configResolver.resolveInt(ConfigPaths.INNER_BORDER_THICKNESS_PATH, InnerBorderConfig.DEFAULT_THICKNESS, InnerBorderConfig::isValidThickness);
        Material block = resolveMaterial(ConfigPaths.INNER_BORDER_BLOCK_PATH, InnerBorderConfig.DEFAULT_BLOCK);

        return new InnerBorderConfig(thickness, block);
    }

    @NonNull
    private FrameConfig getFrameConfig() {
        int thickness = configResolver.resolveInt(ConfigPaths.FRAME_THICKNESS_PATH, FrameConfig.DEFAULT_THICKNESS, FrameConfig::isValidThickness);
        Material block = resolveMaterial(ConfigPaths.FRAME_BLOCK_PATH, FrameConfig.DEFAULT_BLOCK);

        return new FrameConfig(thickness, block);
    }

    private void saveSquareConfig(@NonNull SquareConfig squareConfig) {
        userDataConfigManager.set(ConfigPaths.ROW_COUNT_PATH, squareConfig.rowCount());
        userDataConfigManager.set(ConfigPaths.COL_COUNT_PATH, squareConfig.colCount());
        userDataConfigManager.set(ConfigPaths.WIDTH_PATH, squareConfig.width());
        userDataConfigManager.set(ConfigPaths.HEIGHT_PATH, squareConfig.height());
        userDataConfigManager.set(ConfigPaths.BLACK_BLOCK_PATH, squareConfig.blackBlock().name());
        userDataConfigManager.set(ConfigPaths.WHITE_BLOCK_PATH, squareConfig.whiteBlock().name());
    }

    private void saveInnerBorderConfig(@NonNull InnerBorderConfig innerBorderConfig) {
        userDataConfigManager.set(ConfigPaths.INNER_BORDER_THICKNESS_PATH, innerBorderConfig.thickness());
        userDataConfigManager.set(ConfigPaths.INNER_BORDER_BLOCK_PATH, innerBorderConfig.block().name());
    }

    private void saveFrameConfig(@NonNull FrameConfig frameConfig) {
        userDataConfigManager.set(ConfigPaths.FRAME_THICKNESS_PATH, frameConfig.thickness());
        userDataConfigManager.set(ConfigPaths.FRAME_BLOCK_PATH, frameConfig.block().name());
    }

    @NonNull
    private Material resolveMaterial(@NonNull String path, @NonNull Material fallback) {
        return configResolver.resolve(path, fallback, this::parseMaterial);
    }

    @Nullable
    private Material parseMaterial(@NonNull String materialName) {
        Material material = Material.matchMaterial(materialName);

        return (material != null && material.isBlock()) ? material : null;
    }
}
