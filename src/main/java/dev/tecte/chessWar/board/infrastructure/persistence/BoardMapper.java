package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.infrastructure.persistence.YmlMapper;
import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.COL_COUNT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.HEIGHT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.ROW_COUNT;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.THICKNESS;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.Config.WIDTH;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.State.ANCHOR;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.State.BOUNDING_BOX;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.State.FRAME;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.State.ID;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.State.INNER_BORDER;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.State.ORIENTATION;
import static dev.tecte.chessWar.board.infrastructure.persistence.BoardConstants.State.SQUARE_GRID;

/**
 * {@link Board} 도메인 객체와 YML 파일 데이터 간의 변환을 담당하는 매퍼 클래스입니다.
 */
@Singleton
public class BoardMapper implements YmlMapper<UUID, Board> {
    @NonNull
    @Override
    public Map<String, Object> toMap(@NonNull Board entity) {
        Map<String, Object> map = new HashMap<>();

        map.put(ID, entity.id().toString());
        map.put(SQUARE_GRID, toMapSquareGrid(entity.squareGrid()));
        map.put(INNER_BORDER, toMapBorder(entity.innerBorder()));
        map.put(FRAME, toMapBorder(entity.frame()));

        return map;
    }

    @NonNull
    @Override
    public Board fromSection(@NonNull UUID key, @NonNull ConfigurationSection section) {
        ConfigurationSection squareGridSection = requireSection(section, SQUARE_GRID);
        ConfigurationSection innerBorderSection = requireSection(section, INNER_BORDER);
        ConfigurationSection frameSection = requireSection(section, FRAME);

        return Board.builder()
                .id(key)
                .squareGrid(fromSectionSquareGrid(squareGridSection))
                .innerBorder(fromSectionBorder(innerBorderSection))
                .frame(fromSectionBorder(frameSection))
                .build();
    }

    @NonNull
    private Map<String, Object> toMapSquareGrid(@NonNull SquareGrid squareGrid) {
        Map<String, Object> map = new HashMap<>();

        map.put(ANCHOR, squareGrid.getAnchor());
        map.put(ORIENTATION, squareGrid.getOrientation().name());
        map.put(ROW_COUNT, squareGrid.getRowCount());
        map.put(COL_COUNT, squareGrid.getColCount());
        map.put(WIDTH, squareGrid.getSquareWidth());
        map.put(HEIGHT, squareGrid.getSquareHeight());

        return map;
    }

    @NonNull
    private SquareGrid fromSectionSquareGrid(@NonNull ConfigurationSection section) {
        Vector anchor = requireValue(section, ANCHOR, Vector.class);
        Orientation orientation = requireOrientation(section);
        int rowCount = requireValue(section, ROW_COUNT, Integer.class);
        int colCount = requireValue(section, COL_COUNT, Integer.class);
        int width = requireValue(section, WIDTH, Integer.class);
        int height = requireValue(section, HEIGHT, Integer.class);

        return SquareGrid.create(anchor, orientation, rowCount, colCount, width, height);
    }

    @NonNull
    private Map<String, Object> toMapBorder(@NonNull Border border) {
        Map<String, Object> map = new HashMap<>();

        map.put(BOUNDING_BOX, border.boundingBox());
        map.put(THICKNESS, border.thickness());

        return map;
    }

    @NonNull
    private Border fromSectionBorder(@NonNull ConfigurationSection section) {
        BoundingBox boundingBox = requireValue(section, BOUNDING_BOX, BoundingBox.class);
        int thickness = requireValue(section, THICKNESS, Integer.class);

        return new Border(boundingBox, thickness);
    }

    @NonNull
    private ConfigurationSection requireSection(@NonNull ConfigurationSection parent, @NonNull String key) {
        return Optional.ofNullable(parent.getConfigurationSection(key))
                .orElseThrow(() -> new YmlMappingException("Missing required section: '" + key + "' in '" + parent.getCurrentPath() + "'."));
    }

    @NonNull
    private <T> T requireValue(@NonNull ConfigurationSection section, @NonNull String key, @NonNull Class<T> type) {
        if (!section.contains(key)) {
            throw new YmlMappingException("Missing required value: '" + key + "' in '" + section.getCurrentPath() + "'.");
        }

        T value = section.getObject(key, type);

        if (value == null) {
            Object rawValue = section.get(key);
            String actualType = rawValue != null ? rawValue.getClass().getSimpleName() : "null";

            throw new YmlMappingException("Invalid type for '" + key + "' in '" + section.getCurrentPath() + "'. Expected " + type.getSimpleName() + ", but found " + actualType + ".");
        }

        return value;
    }

    @NonNull
    private Orientation requireOrientation(@NonNull ConfigurationSection section) {
        String orientationName = requireValue(section, ORIENTATION, String.class);

        try {
            return Orientation.valueOf(orientationName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new YmlMappingException("Invalid 'orientation' value in '" + section.getCurrentPath() + "': " + orientationName);
        }
    }
}
