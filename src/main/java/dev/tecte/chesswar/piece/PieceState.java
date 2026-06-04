package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(staticName = "create")
public class PieceState {
    private final Map<Coordinate, Piece> boardPieces = new HashMap<>();
    private final Map<Coordinate, LivingEntity> pieceEntities = new HashMap<>();
    private final Map<UUID, Coordinate> entityToCoordinate = new HashMap<>();
    private final Set<UUID> spawnedEntities = new HashSet<>();

    public void reset() {
        boardPieces.clear();
        pieceEntities.clear();
        entityToCoordinate.clear();
        spawnedEntities.clear();
    }
}
