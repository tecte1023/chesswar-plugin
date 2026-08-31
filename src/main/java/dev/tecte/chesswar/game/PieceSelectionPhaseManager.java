package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.BoardComponent;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.Grid;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.TeamManager;
import dev.tecte.chesswar.team.TeamSide;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public final class PieceSelectionPhaseManager {
    private final Piece[] occupancy;

    @NotNull
    private final GamePhaseComponent phaseComponent;

    @NotNull
    private final BoardComponent boardComponent;

    @NotNull
    private final PieceManager pieceManager;

    @NotNull
    private final TeamManager teamManager;

    @NotNull
    private final PieceSelectionPhasePresenter presenter;

    public void inspectPiece(@NotNull final Player player, @NotNull final UUID entityId) {
        if (phaseComponent.phase() != GamePhase.PIECE_SELECTION) {
            return;
        }

        final Coordinate coordinate = pieceManager.findCoordinate(entityId);

        if (coordinate == null) {
            return;
        }

        final TeamSide playerTeamSide = teamManager.findTeamSide(player.getUniqueId());

        if (playerTeamSide == null) {
            return;
        }

        presenter.showPieceDescription(player, coordinate, playerTeamSide);
    }

    public void trySelectPiece(@NotNull final Player player, @NotNull final Coordinate coordinate) {
        if (phaseComponent.phase() != GamePhase.PIECE_SELECTION) {
            return;
        }

        final TeamSide playerTeamSide = teamManager.findTeamSide(player.getUniqueId());

        if (playerTeamSide == null) {
            return;
        }

        final Piece targetPiece = occupancy[coordinate.flatIndex()];

        if (targetPiece == null) {
            return;
        }

        if (targetPiece.teamSide() != playerTeamSide) {
            return;
        }

        if (targetPiece.type() == PieceType.PAWN) {
            return;
        }

        if (targetPiece.isPlayer()) {
            if (!targetPiece.id().equals(player.getUniqueId())) {
                presenter.showPieceAlreadyTakenFeedback(player);
            }

            return;
        }

        final Coordinate previousCoordinate = pieceManager.findCoordinate(player.getUniqueId());

        if (previousCoordinate != null) {
            final Grid barracksGrid = boardComponent.board().getBarracks(playerTeamSide).grid();
            final Location spawnLocation = barracksGrid.getCenterAt(previousCoordinate);

            spawnLocation.setYaw(barracksGrid.anchor().getYaw() + playerTeamSide.yawOffset());
            pieceManager.forceUnassignPlayer(previousCoordinate, spawnLocation);
        }

        pieceManager.forceAssignPlayer(coordinate, player.getUniqueId());
        presenter.showPieceSelectedFeedback(player);
    }
}
