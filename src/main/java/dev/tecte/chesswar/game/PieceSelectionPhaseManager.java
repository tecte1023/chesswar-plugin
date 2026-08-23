package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.TeamManager;
import dev.tecte.chesswar.team.TeamSide;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public final class PieceSelectionPhaseManager {
    @NotNull
    private final GamePhaseComponent phaseComponent;

    @NotNull
    private final PieceManager pieceManager;

    @NotNull
    private final TeamManager teamManager;

    @Nullable
    public PieceInspectionResult tryInspectPiece(@NotNull final Player player, @NotNull final UUID entityId) {
        if (phaseComponent.phase() != GamePhase.PIECE_SELECTION) {
            return null;
        }

        final Piece piece = pieceManager.findPiece(entityId);

        if (piece == null) {
            return null;
        }

        final TeamSide playerTeamSide = teamManager.findTeamSide(player.getUniqueId());

        if (playerTeamSide == null) {
            return null;
        }

        final PieceType type = piece.type();
        
        if (type == PieceType.PAWN) {
            return new PieceInspectionResult(type, PieceSelectability.UNSELECTABLE_TYPE);
        }
        
        if (piece.teamSide() != playerTeamSide) {
            return new PieceInspectionResult(type, PieceSelectability.UNSELECTABLE_TEAM);
        }
        
        return new PieceInspectionResult(type, PieceSelectability.SELECTABLE);
    }
}
