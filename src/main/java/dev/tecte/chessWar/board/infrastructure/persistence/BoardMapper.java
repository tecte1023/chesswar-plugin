package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.BorderType;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.board.domain.model.spec.BorderSpec;
import dev.tecte.chessWar.board.domain.model.spec.GridSpec;
import dev.tecte.chessWar.board.domain.model.spec.SquareSpec;
import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.port.persistence.SingleYmlMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static dev.tecte.chessWar.board.infrastructure.persistence.BoardPersistenceConstants.Keys;

/**
 * {@link Board} 도메인 객체와 YML 파일 데이터 간의 변환을 담당하는 매퍼 클래스입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardMapper implements SingleYmlMapper<Board> {
    private final SquareSpec squareSpec;
    private final GridSpec gridSpec;
    private final BorderSpec borderSpec;
    private final YmlParser parser;

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Map<String, Object> toMap(@NonNull Board entity) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.WORLD_NAME, entity.worldName());
        map.put(Keys.SQUARE_GRID, toMapSquareGrid(entity.squareGrid()));
        map.put(Keys.INNER_BORDER, toMapBorder(entity.innerBorder()));
        map.put(Keys.FRAME, toMapBorder(entity.frame()));

        return map;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Board fromSection(@NonNull ConfigurationSection section) {
        String worldName = parser.requireValue(section, Keys.WORLD_NAME, String.class);
        ConfigurationSection squareGridSection = parser.requireSection(section, Keys.SQUARE_GRID);
        ConfigurationSection innerBorderSection = parser.requireSection(section, Keys.INNER_BORDER);
        ConfigurationSection frameSection = parser.requireSection(section, Keys.FRAME);

        return Board.builder()
                .worldName(worldName)
                .squareGrid(fromSectionSquareGrid(squareGridSection))
                .innerBorder(fromSectionBorder(innerBorderSection, borderSpec.innerBorderType()))
                .frame(fromSectionBorder(frameSection, borderSpec.frameBorderType()))
                .build();
    }

    @NonNull
    private Map<String, Object> toMapSquareGrid(@NonNull SquareGrid squareGrid) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.ANCHOR, squareGrid.anchor());
        map.put(Keys.ORIENTATION, squareGrid.orientation().name());

        return map;
    }

    @NonNull
    private SquareGrid fromSectionSquareGrid(@NonNull ConfigurationSection section) {
        Vector anchor = parser.requireValue(section, Keys.ANCHOR, Vector.class);
        Orientation orientation = parser.requireEnum(section, Keys.ORIENTATION, Orientation::from);

        return SquareGrid.builder()
                .anchor(anchor)
                .orientation(orientation)
                .gridSpec(gridSpec)
                .squareSpec(squareSpec)
                .build();
    }

    @NonNull
    private Map<String, Object> toMapBorder(@NonNull Border border) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.BOUNDING_BOX, border.boundingBox());

        return map;
    }

    @NonNull
    private Border fromSectionBorder(@NonNull ConfigurationSection section, @NonNull BorderType borderType) {
        BoundingBox boundingBox = parser.requireValue(section, Keys.BOUNDING_BOX, BoundingBox.class);

        return new Border(borderType, boundingBox, borderSpec.thickness(borderType));
    }
}
