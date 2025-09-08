package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.infrastructure.persistence.YmlMapper;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
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
 * {@link Board} 도메인 객체와 YML 파일에 저장될 Map 형태 간의 변환을 담당하는 매퍼 클래스입니다.
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
    @SuppressWarnings("unchecked")
    public Board fromMap(@NonNull UUID key, @NonNull Map<String, Object> map) {
        return dev.tecte.chessWar.board.domain.model.Board.builder()
                .id(key)
                .squareGrid(fromMapSquareGrid((Map<String, Object>) map.get(SQUARE_GRID)))
                .innerBorder(fromMapBorder((Map<String, Object>) map.get(INNER_BORDER)))
                .frame(fromMapBorder((Map<String, Object>) map.get(FRAME)))
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
    @SuppressWarnings("unchecked")
    private SquareGrid fromMapSquareGrid(@NonNull Map<String, Object> map) {
        return SquareGrid.create(
                Vector.deserialize((Map<String, Object>) map.get(ANCHOR)),
                Orientation.valueOf((String) map.get(ORIENTATION)),
                (int) map.get(ROW_COUNT),
                (int) map.get(COL_COUNT),
                (int) map.get(WIDTH),
                (int) map.get(HEIGHT)
        );
    }

    @NonNull
    private Map<String, Object> toMapBorder(@NonNull Border border) {
        Map<String, Object> map = new HashMap<>();

        map.put(BOUNDING_BOX, border.boundingBox());
        map.put(THICKNESS, border.thickness());

        return map;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private Border fromMapBorder(@NonNull Map<String, Object> map) {
        return new Border(
                BoundingBox.deserialize((Map<String, Object>) map.get(BOUNDING_BOX)),
                (int) map.get(THICKNESS)
        );
    }
}
