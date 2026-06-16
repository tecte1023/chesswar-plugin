package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.entity.LivingEntity;

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
    private final Map<Coordinate, Piece> boardPieces = new HashMap<>();
    private final Map<Coordinate, LivingEntity> pieceEntities = new HashMap<>();
    private final Map<UUID, Coordinate> entityToCoordinate = new HashMap<>();
    private final Set<UUID> spawnedEntities = new HashSet<>();
    private final Map<Team, Map<PieceType, StatBuff>> teamBuffs = new EnumMap<>(Team.class);

    public StatBuff getBuff(Team team, PieceType type) {
        return teamBuffs.computeIfAbsent(team, k -> new EnumMap<>(PieceType.class))
                .computeIfAbsent(type, k -> StatBuff.create());
    }

    public void reset() {
        boardPieces.values().removeIf(piece -> piece.ownerId() == null);

        final Set<UUID> playerIds = new HashSet<>();
        for (final Piece piece : boardPieces.values()) {
            if (piece.ownerId() != null) {
                playerIds.add(piece.ownerId());
            }
        }

        pieceEntities.entrySet().removeIf(entry -> !playerIds.contains(entry.getValue().getUniqueId()));
        entityToCoordinate.keySet().removeIf(uuid -> !playerIds.contains(uuid));
        spawnedEntities.clear();
        teamBuffs.clear();
    }
}
