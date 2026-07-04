package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(staticName = "create")
public class PieceState {
    @NotNull
    private final Piece[] boardOccupancy = new Piece[Coordinate.SQUARE_COUNT];
    @NotNull
    private final Map<UUID, Coordinate> entityToCoordinate = new HashMap<>();
    @NotNull
    private final Set<UUID> spawnedEntities = new HashSet<>();
    @NotNull
    private final Map<Team, Map<PieceType, StatBuff>> teamBuffs = new EnumMap<>(Team.class);
    private int occupancyVersion = 0;

    @Nullable
    public Coordinate coordinate(@NotNull final UUID id) {
        return entityToCoordinate.get(id);
    }

    @Nullable
    public Piece piece(@NotNull final Coordinate coordinate) {
        return boardOccupancy[coordinate.flatIndex()];
    }

    public boolean hasPiece(@NotNull final Coordinate coordinate) {
        return boardOccupancy[coordinate.flatIndex()] != null;
    }

    boolean addPiece(@NotNull final Coordinate coordinate, @NotNull final Piece piece) {
        final int index = coordinate.flatIndex();
        if (boardOccupancy[index] != null) {
            return false;
        }
        boardOccupancy[index] = piece;
        entityToCoordinate.put(piece.id(), coordinate);
        occupancyVersion++;
        return true;
    }

    boolean removePiece(@NotNull final Coordinate coordinate) {
        final int index = coordinate.flatIndex();
        final Piece piece = boardOccupancy[index];
        if (piece == null) {
            return false;
        }
        boardOccupancy[index] = null;
        entityToCoordinate.remove(piece.id());
        occupancyVersion++;
        return true;
    }

    boolean movePiece(@NotNull final Coordinate from, @NotNull final Coordinate to) {
        final int fromIndex = from.flatIndex();
        final int toIndex = to.flatIndex();
        final Piece piece = boardOccupancy[fromIndex];
        if (piece == null || boardOccupancy[toIndex] != null) {
            return false;
        }
        boardOccupancy[fromIndex] = null;
        boardOccupancy[toIndex] = piece;
        entityToCoordinate.put(piece.id(), to);
        occupancyVersion++;
        return true;
    }

    void purgeEntity(@NotNull final UUID id) {
        spawnedEntities.remove(id);
        entityToCoordinate.remove(id);
    }

    void reset() {
        java.util.Arrays.fill(boardOccupancy, null);
        entityToCoordinate.clear();
        spawnedEntities.clear();
        teamBuffs.clear();
        occupancyVersion++;
    }
}
