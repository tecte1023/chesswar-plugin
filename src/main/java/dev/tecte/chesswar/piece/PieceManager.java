package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.economy.GoldComponent;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.GamePhaseComponent;
import dev.tecte.chesswar.team.TeamSide;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public final class PieceManager {
    private final Piece[] occupancy;

    @NotNull
    private final GamePhaseComponent phaseComponent;

    @NotNull
    private final PiecePresenter presenter;

    public void forceSpawnPiece(
            @NotNull final Coordinate coordinate,
            @NotNull final PieceType type,
            @NotNull final TeamSide teamSide,
            @NotNull final Location location
    ) {
        final UUID entityId = presenter.showPiece(teamSide, type, location);

        if (entityId == null) {
            return;
        }

        final double baseHealth = type.baseHealth();
        final var stat = new StatComponent(baseHealth, baseHealth, type.baseDamage());
        final var actionMask = new ActionMaskComponent(0L, 0L, -1);
        final var effect = new EffectComponent(0L, new int[EffectType.COUNT]);
        final var ability = new AbilityComponent(0L);
        final var gold = new GoldComponent(0);
        final var piece = new Piece(entityId, teamSide, type, false, stat, actionMask, effect, ability, gold);
        final int index = coordinate.flatIndex();

        if (occupancy[index] != null) {
            presenter.hidePiece(occupancy[index].id());
        }

        occupancy[index] = piece;
    }

    public void clearAllPieces() {
        for (int i = 0; i < occupancy.length; i++) {
            if (occupancy[i] == null) {
                continue;
            }

            presenter.hidePiece(occupancy[i].id());
            occupancy[i] = null;
        }
    }

    @Nullable
    public Piece findPiece(@NotNull final UUID entityId) {
        for (final Piece piece : occupancy) {
            if (piece == null) {
                continue;
            }

            if (piece.id().equals(entityId)) {
                return piece;
            }
        }

        return null;
    }

    @Nullable
    public Coordinate findCoordinate(@NotNull final UUID entityId) {
        for (int i = 0; i < occupancy.length; i++) {
            final Piece piece = occupancy[i];

            if (piece != null && piece.id().equals(entityId)) {
                return Coordinate.fromFlatIndex(i);
            }
        }

        return null;
    }

    public void forceAssignPlayer(@NotNull final Coordinate coordinate, @NotNull final UUID playerId) {
        final int index = coordinate.flatIndex();
        final Piece oldPiece = occupancy[index];

        if (oldPiece == null || oldPiece.isPlayer()) {
            return;
        }

        presenter.hidePiece(oldPiece.id());
        occupancy[index] = new Piece(
                playerId,
                oldPiece.teamSide(),
                oldPiece.type(),
                true,
                oldPiece.stat(),
                oldPiece.actionMask(),
                oldPiece.effect(),
                oldPiece.ability(),
                oldPiece.gold()
        );
    }

    public void forceUnassignPlayer(
            @NotNull final Coordinate coordinate,
            @NotNull final Location spawnLocation
    ) {
        final int index = coordinate.flatIndex();
        final Piece oldPiece = occupancy[index];

        if (oldPiece == null || !oldPiece.isPlayer()) {
            return;
        }

        final UUID npcId = presenter.showPiece(oldPiece.teamSide(), oldPiece.type(), spawnLocation);

        if (npcId == null) {
            occupancy[index] = null;
            return;
        }

        occupancy[index] = new Piece(
                npcId,
                oldPiece.teamSide(),
                oldPiece.type(),
                false,
                oldPiece.stat(),
                oldPiece.actionMask(),
                oldPiece.effect(),
                oldPiece.ability(),
                oldPiece.gold()
        );
    }

    public boolean isDamageProtected(@NotNull final UUID entityId) {
        if (phaseComponent.phase() == GamePhase.BATTLE) {
            return false;
        }

        return findPiece(entityId) != null;
    }
}
