package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

@RequiredArgsConstructor
public class PiecePdcMapper {
    private static final String KEY_PIECE_TYPE = "chess_piece_type";
    private static final String KEY_PIECE_TEAM = "chess_piece_team";
    private static final String KEY_COORDINATE_X = "chess_piece_x";
    private static final String KEY_COORDINATE_Y = "chess_piece_y";
    private static final String KEY_IS_DISPLAY = "is_display_entity";

    private final NamespacedKey pieceTypeKey;
    private final NamespacedKey pieceTeamKey;
    private final NamespacedKey coordinateXKey;
    private final NamespacedKey coordinateYKey;
    private final NamespacedKey isDisplayKey;

    public static PiecePdcMapper create(final Plugin plugin) {
        return new PiecePdcMapper(
                new NamespacedKey(plugin, KEY_PIECE_TYPE),
                new NamespacedKey(plugin, KEY_PIECE_TEAM),
                new NamespacedKey(plugin, KEY_COORDINATE_X),
                new NamespacedKey(plugin, KEY_COORDINATE_Y),
                new NamespacedKey(plugin, KEY_IS_DISPLAY)
        );
    }

    public void writeData(
            final Entity entity,
            final PieceType type,
            final Team team,
            final Coordinate coordinate,
            final boolean isDisplay
    ) {
        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(pieceTypeKey, PersistentDataType.STRING, type.name());
        pdc.set(pieceTeamKey, PersistentDataType.STRING, team.name());
        pdc.set(coordinateXKey, PersistentDataType.INTEGER, coordinate.x());
        pdc.set(coordinateYKey, PersistentDataType.INTEGER, coordinate.y());
        pdc.set(isDisplayKey, PersistentDataType.BYTE, (byte) (isDisplay ? 1 : 0));
    }

    public void updateCoordinate(final Entity entity, final Coordinate coordinate) {
        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(coordinateXKey, PersistentDataType.INTEGER, coordinate.x());
        pdc.set(coordinateYKey, PersistentDataType.INTEGER, coordinate.y());
    }

    public Optional<Coordinate> readCoordinate(final Entity entity) {
        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        final Integer x = pdc.get(coordinateXKey, PersistentDataType.INTEGER);
        final Integer y = pdc.get(coordinateYKey, PersistentDataType.INTEGER);

        if (x == null || y == null) {
            return Optional.empty();
        }

        return Optional.of(Coordinate.of(x, y));
    }

    public boolean isDisplay(final Entity entity) {
        final Byte isDisplay = entity.getPersistentDataContainer().get(isDisplayKey, PersistentDataType.BYTE);
        return isDisplay != null && isDisplay == 1;
    }
}
