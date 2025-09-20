package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.infrastructure.persistence.YmlMapper;
import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.tecte.chessWar.board.infrastructure.persistence.BoardPersistenceConstants.Keys;

/**
 * {@link Board} 도메인 객체와 YML 파일 데이터 간의 변환을 담당하는 매퍼 클래스입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardMapper implements YmlMapper<UUID, Board> {
    private final YmlParser parser;

    @NonNull
    @Override
    public Map<String, Object> toMap(@NonNull Board entity) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.SQUARE_GRID, toMapSquareGrid(entity.squareGrid()));
        map.put(Keys.INNER_BORDER, toMapBorder(entity.innerBorder()));
        map.put(Keys.FRAME, toMapBorder(entity.frame()));

        return map;
    }

    @NonNull
    @Override
    public Board fromSection(@NonNull UUID key, @NonNull ConfigurationSection section) {
        ConfigurationSection squareGridSection = parser.requireSection(section, Keys.SQUARE_GRID);
        ConfigurationSection innerBorderSection = parser.requireSection(section, Keys.INNER_BORDER);
        ConfigurationSection frameSection = parser.requireSection(section, Keys.FRAME);

        return new Board(
                key,
                fromSectionSquareGrid(squareGridSection),
                fromSectionBorder(innerBorderSection),
                fromSectionBorder(frameSection)
        );
    }

    @NonNull
    private Map<String, Object> toMapSquareGrid(@NonNull SquareGrid squareGrid) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.ANCHOR, squareGrid.getAnchor());
        map.put(Keys.ORIENTATION, squareGrid.getOrientation().name());
        map.put(Keys.ROW_COUNT, squareGrid.getRowCount());
        map.put(Keys.COL_COUNT, squareGrid.getColCount());
        map.put(Keys.WIDTH, squareGrid.getSquareWidth());
        map.put(Keys.HEIGHT, squareGrid.getSquareHeight());

        return map;
    }

    @NonNull
    private SquareGrid fromSectionSquareGrid(@NonNull ConfigurationSection section) {
        Vector anchor = parser.requireValue(section, Keys.ANCHOR, Vector.class);
        Orientation orientation = parser.requireEnum(section, Keys.ORIENTATION, Orientation.class);
        int rowCount = parser.requireValue(section, Keys.ROW_COUNT, Integer.class);
        int colCount = parser.requireValue(section, Keys.COL_COUNT, Integer.class);
        int width = parser.requireValue(section, Keys.WIDTH, Integer.class);
        int height = parser.requireValue(section, Keys.HEIGHT, Integer.class);

        return SquareGrid.create(anchor, orientation, rowCount, colCount, width, height);
    }

    @NonNull
    private Map<String, Object> toMapBorder(@NonNull Border border) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.BOUNDING_BOX, border.boundingBox());
        map.put(Keys.THICKNESS, border.thickness());

        return map;
    }

    @NonNull
    private Border fromSectionBorder(@NonNull ConfigurationSection section) {
        BoundingBox boundingBox = parser.requireValue(section, Keys.BOUNDING_BOX, BoundingBox.class);
        int thickness = parser.requireValue(section, Keys.THICKNESS, Integer.class);

        return new Border(boundingBox, thickness);
    }
}
