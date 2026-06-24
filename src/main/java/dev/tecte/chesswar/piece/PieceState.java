package dev.tecte.chesswar.piece;

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
    private final Piece[][] boardPieces = new Piece[Coordinate.BOARD_SIZE][Coordinate.BOARD_SIZE];
    @NotNull
    private final Map<UUID, Coordinate> entityToCoordinate = new HashMap<>();
    @NotNull
    private final Set<UUID> spawnedEntities = new HashSet<>();
    @NotNull
    private final Map<Team, Map<PieceType, StatBuff>> teamBuffs = new EnumMap<>(Team.class);

    @NotNull
    public StatBuff getBuff(@NotNull Team team, @NotNull PieceType type) {
        return teamBuffs.computeIfAbsent(team, k -> new EnumMap<>(PieceType.class))
                .computeIfAbsent(type, k -> StatBuff.create());
    }

    public boolean addPiece(@NotNull final Coordinate coord, @NotNull final Piece piece) {
        if (hasPiece(coord)) {
            return false;
        }

        boardPieces[coord.x()][coord.y()] = piece;
        entityToCoordinate.put(piece.id(), coord);

        return true;
    }

    public boolean movePiece(@NotNull final Coordinate from, @NotNull final Coordinate to) {
        if (!hasPiece(from) || hasPiece(to)) {
            return false;
        }

        final Piece piece = boardPieces[from.x()][from.y()];
        boardPieces[from.x()][from.y()] = null;
        boardPieces[to.x()][to.y()] = piece;

        if (piece != null) {
            entityToCoordinate.put(piece.id(), to);
        }

        return true;
    }

    public boolean removePiece(@NotNull final Coordinate coord) {
        final Piece piece = boardPieces[coord.x()][coord.y()];

        if (piece == null) {
            return false;
        }

        boardPieces[coord.x()][coord.y()] = null;
        entityToCoordinate.remove(piece.id());

        return true;
    }

    public void purgeEntity(@NotNull final UUID id) {
        final Coordinate coord = entityToCoordinate.remove(id);

        if (coord != null) {
            final Piece piece = boardPieces[coord.x()][coord.y()];

            if (piece != null && id.equals(piece.id())) {
                boardPieces[coord.x()][coord.y()] = null;
            }
        }

        spawnedEntities.remove(id);
    }

    @Nullable
    public Coordinate coordinate(@NotNull final UUID id) {
        return entityToCoordinate.get(id);
    }

    @Nullable
    public Piece piece(@NotNull final Coordinate coordinate) {
        return boardPieces[coordinate.x()][coordinate.y()];
    }

    public boolean hasPiece(@NotNull final Coordinate coordinate) {
        return boardPieces[coordinate.x()][coordinate.y()] != null;
    }

    public void reset() {
        for (int x = 0; x < Coordinate.BOARD_SIZE; x++) {
            for (int y = 0; y < Coordinate.BOARD_SIZE; y++) {
                final Piece piece = boardPieces[x][y];

                if (piece != null && !piece.isPlayer()) {
                    removePiece(Coordinate.of(x, y));
                }
            }
        }

        spawnedEntities.clear();
        teamBuffs.clear();
    }
}
