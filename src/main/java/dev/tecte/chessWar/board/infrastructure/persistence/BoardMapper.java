package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.BorderType;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.board.domain.model.spec.BorderSpec;
import dev.tecte.chessWar.board.domain.model.spec.GridSpec;
import dev.tecte.chessWar.board.domain.model.spec.SquareSpec;
import dev.tecte.chessWar.board.infrastructure.persistence.BoardPersistenceConstants.Keys;
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

/**
 * 체스판과 YAML 데이터 간 변환을 수행합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardMapper {
    private final SquareSpec squareSpec;
    private final GridSpec gridSpec;
    private final BorderSpec borderSpec;
    private final YmlParser parser;

    /**
     * 체스판을 YAML 데이터 맵으로 변환합니다.
     *
     * @param board 변환할 체스판
     * @return YAML 데이터 맵
     */
    @NonNull
    public Map<String, Object> toMap(@NonNull Board board) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.WORLD_NAME, board.worldName());
        map.put(Keys.SQUARE_GRID, toMapSquareGrid(board.squareGrid()));
        map.put(Keys.INNER_BORDER, toMapBorder(board.innerBorder()));
        map.put(Keys.FRAME, toMapBorder(board.frame()));

        return map;
    }

    /**
     * YAML 데이터로부터 체스판을 복원합니다.
     *
     * @param section 데이터 섹션
     * @return 복원된 체스판
     */
    @NonNull
    public Board fromSection(@NonNull ConfigurationSection section) {
        String worldName = parser.requireValue(section, Keys.WORLD_NAME, String.class);
        ConfigurationSection squareGridSection = parser.requireSection(section, Keys.SQUARE_GRID);
        ConfigurationSection innerBorderSection = parser.requireSection(section, Keys.INNER_BORDER);
        ConfigurationSection frameSection = parser.requireSection(section, Keys.FRAME);

        return Board.builder()
                .worldName(worldName)
                .squareGrid(fromSectionSquareGrid(squareGridSection))
                .innerBorder(fromSectionBorder(innerBorderSection, BorderType.INNER_BORDER))
                .frame(fromSectionBorder(frameSection, BorderType.FRAME))
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
    private Map<String, Object> toMapBorder(@NonNull Border border) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.BOUNDING_BOX, border.boundingBox());

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
    private Border fromSectionBorder(@NonNull ConfigurationSection section, @NonNull BorderType borderType) {
        BoundingBox boundingBox = parser.requireValue(section, Keys.BOUNDING_BOX, BoundingBox.class);

        return new Border(borderType, boundingBox, borderSpec.thickness(borderType));
    }
}
