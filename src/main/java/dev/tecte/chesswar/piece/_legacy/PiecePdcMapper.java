/*
package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class PiecePdcMapper {
    private static final String KEY_PIECE_TYPE = "piece_type";
    private static final String KEY_PIECE_TEAM = "piece_team";
    private static final String KEY_COORD_X = "piece_coord_x";
    private static final String KEY_COORD_Y = "piece_coord_y";
    private static final String KEY_IS_DISPLAY = "is_display_piece";

    private final NamespacedKey pieceTypeKey;
    private final NamespacedKey pieceTeamKey;
    private final NamespacedKey coordXKey;
    private final NamespacedKey coordYKey;
    private final NamespacedKey isDisplayKey;

    public static PiecePdcMapper create(final Plugin plugin) {
        return new PiecePdcMapper(
                new NamespacedKey(plugin, KEY_PIECE_TYPE),
                new NamespacedKey(plugin, KEY_PIECE_TEAM),
                new NamespacedKey(plugin, KEY_COORD_X),
                new NamespacedKey(plugin, KEY_COORD_Y),
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
        pdc.set(coordXKey, PersistentDataType.INTEGER, coordinate.x());
        pdc.set(coordYKey, PersistentDataType.INTEGER, coordinate.y());
        pdc.set(isDisplayKey, PersistentDataType.BYTE, (byte) (isDisplay ? 1 : 0));
    }

    public void updateCoordinate(final Entity entity, final Coordinate coordinate) {
        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(coordXKey, PersistentDataType.INTEGER, coordinate.x());
        pdc.set(coordYKey, PersistentDataType.INTEGER, coordinate.y());
    }

    @Nullable
    public Coordinate readCoordinate(final Entity entity) {
        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        final Integer x = pdc.get(coordXKey, PersistentDataType.INTEGER);
        final Integer y = pdc.get(coordYKey, PersistentDataType.INTEGER);

        if (x == null || y == null) {
            return null;
        }

        return Coordinate.of(x, y);
    }

    @Nullable
    public PieceType readType(final Entity entity) {
        final String typeStr = entity.getPersistentDataContainer().get(pieceTypeKey, PersistentDataType.STRING);
        if (typeStr == null) {
            return null;
        }
        try {
            return PieceType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    public Team readTeam(final Entity entity) {
        final String teamStr = entity.getPersistentDataContainer().get(pieceTeamKey, PersistentDataType.STRING);
        if (teamStr == null) {
            return null;
        }
        try {
            return Team.valueOf(teamStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isDisplay(final Entity entity) {
        final Byte isDisplay = entity.getPersistentDataContainer().get(isDisplayKey, PersistentDataType.BYTE);
        return isDisplay != null && isDisplay == 1;
    }
}
*/
