package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.TeamSide;
import org.jetbrains.annotations.NotNull;

public enum PieceSelectability {
    SELECTABLE,
    UNSELECTABLE_TEAM,
    UNSELECTABLE_TYPE;

    @NotNull
    public static PieceSelectability evaluate(
            @NotNull final PieceType type,
            @NotNull final TeamSide ownerTeam,
            @NotNull final TeamSide viewerTeam
    ) {
        if (type == PieceType.PAWN) {
            return UNSELECTABLE_TYPE;
        }

        if (ownerTeam != viewerTeam) {
            return UNSELECTABLE_TEAM;
        }

        return SELECTABLE;
    }
}
