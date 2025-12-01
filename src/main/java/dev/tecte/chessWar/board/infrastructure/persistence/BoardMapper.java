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
import dev.tecte.chessWar.port.persistence.SingleYmlMapper;
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
 * {@link Board} 도메인 객체와 YML 파일 데이터 간의 변환을 담당하는 매퍼 클래스입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardMapper implements SingleYmlMapper<Board> {
    private static final int COORDINATE_NOTATION_LENGTH = 2;
    private static final String EXPECTED_NOTATION_RANGE = "A1-H8";

    private final SquareSpec squareSpec;
    private final GridSpec gridSpec;
    private final BorderSpec borderSpec;
    private final YmlParser parser;

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

    /**
     * 좌표 객체를 표준 체스 기보 표기법 형식의 문자열로 변환합니다.
     * 열은 알파벳으로, 행은 숫자로 변환됩니다.
     *
     * @param coordinate 변환할 좌표 객체
     * @return 변환된 좌표 문자열
     */
    @NonNull
    public String serializeCoordinate(@NonNull Coordinate coordinate) {
        char file = (char) ('A' + coordinate.col());
        char rank = (char) ('1' + coordinate.row());

        return "" + file + rank;
    }

    /**
     * 표준 체스 기보 표기법 형식의 문자열을 좌표 객체로 변환합니다.
     * 첫 번째 글자는 열, 두 번째 글자는 행을 나타냅니다.
     *
     * @param key  변환할 좌표 문자열
     * @param path 오류 발생 시 로그에 남길 YML 경로 정보
     * @return 변환된 {@link Coordinate} 객체
     * @throws YmlMappingException 문자열 길이가 맞지 않거나, 파싱할 수 없는 형식일 경우
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
