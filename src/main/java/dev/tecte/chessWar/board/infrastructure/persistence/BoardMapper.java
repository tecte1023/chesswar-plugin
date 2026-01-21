package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.BorderType;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.board.domain.model.spec.BorderSpec;
import dev.tecte.chessWar.board.domain.model.spec.GridSpec;
import dev.tecte.chessWar.board.domain.model.spec.SquareSpec;
import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static dev.tecte.chessWar.board.infrastructure.persistence.BoardPersistenceConstants.Keys;

/**
 * 체스판 객체와 YML 데이터 간의 변환을 담당하는 매퍼입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardMapper {
    private static final int COORDINATE_NOTATION_LENGTH = 2;
    private static final String EXPECTED_NOTATION_RANGE = "A1-H8";

    private final SquareSpec squareSpec;
    private final GridSpec gridSpec;
    private final BorderSpec borderSpec;
    private final YmlParser parser;

    /**
     * 체스판 객체를 YML 저장 가능한 맵 형태로 변환합니다.
     *
     * @param entity 변환할 체스판 엔티티
     * @return 직렬화된 데이터 맵
     */
    @NonNull
    public Map<String, Object> toMap(@NonNull Board entity) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.WORLD_NAME, entity.worldName());
        map.put(Keys.SQUARE_GRID, toMapSquareGrid(entity.squareGrid()));
        map.put(Keys.INNER_BORDER, toMapBorder(entity.innerBorder()));
        map.put(Keys.FRAME, toMapBorder(entity.frame()));

        return map;
    }

    /**
     * YML 섹션 데이터로부터 체스판 객체를 복원합니다.
     *
     * @param section 데이터가 담긴 섹션
     * @return 복원된 체스판 객체
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
                .innerBorder(fromSectionBorder(innerBorderSection, borderSpec.innerBorderType()))
                .frame(fromSectionBorder(frameSection, borderSpec.frameBorderType()))
                .build();
    }

    /**
     * 좌표 객체를 표준 체스 기보 표기법(예: A1)으로 변환합니다.
     *
     * @param coordinate 변환할 좌표
     * @return 기보 표기 문자열
     */
    @NonNull
    public String serializeCoordinate(@NonNull Coordinate coordinate) {
        char file = (char) ('A' + coordinate.col());
        char rank = (char) ('1' + coordinate.row());

        return "" + file + rank;
    }

    /**
     * 체스 기보 표기 문자열을 좌표 객체로 변환합니다.
     *
     * @param key  좌표 문자열 (예: A1)
     * @param path 로그용 YML 경로
     * @return 변환된 좌표 객체
     * @throws YmlMappingException 형식이 잘못되었거나 범위를 벗어난 경우
     */
    @NonNull
    public Coordinate deserializeCoordinate(@NonNull String key, @Nullable String path) {
        if (key.length() != COORDINATE_NOTATION_LENGTH) {
            throw YmlMappingException.forLengthMismatch(path, COORDINATE_NOTATION_LENGTH, key);
        }

        try {
            int col = Character.toUpperCase(key.charAt(0)) - 'A';
            int row = key.charAt(1) - '1';

            return new Coordinate(row, col);
        } catch (IllegalArgumentException e) {
            throw YmlMappingException.forValueOutOfBounds(path, EXPECTED_NOTATION_RANGE, key, e);
        }
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
