package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.team.TeamSide;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.tecte.chesswar.piece.PieceType.BISHOP;
import static dev.tecte.chesswar.piece.PieceType.KING;
import static dev.tecte.chesswar.piece.PieceType.KNIGHT;
import static dev.tecte.chesswar.piece.PieceType.PAWN;
import static dev.tecte.chesswar.piece.PieceType.QUEEN;
import static dev.tecte.chesswar.piece.PieceType.ROOK;
import static dev.tecte.chesswar.team.TeamSide.BLACK;
import static dev.tecte.chesswar.team.TeamSide.WHITE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InitialLayoutPolicy {
    private static final PieceType[] INITIAL_TYPES = {
            ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK,
            PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN,
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN,
            ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK
    };
    private static final TeamSide[] INITIAL_TEAMS = {
            BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK,
            BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK,
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            WHITE, WHITE, WHITE, WHITE, WHITE, WHITE, WHITE, WHITE,
            WHITE, WHITE, WHITE, WHITE, WHITE, WHITE, WHITE, WHITE
    };

    @Nullable
    public static PieceType initialPieceType(@NotNull final Coordinate coordinate) {
        return INITIAL_TYPES[visualIndex(coordinate)];
    }

    @Nullable
    public static TeamSide teamSide(@NotNull final Coordinate coordinate) {
        return INITIAL_TEAMS[visualIndex(coordinate)];
    }

    private static int visualIndex(@NotNull final Coordinate coordinate) {
        final int visualY = Coordinate.BOARD_SIZE - 1 - coordinate.y();

        return Coordinate.flatten(coordinate.x(), visualY);
    }
}
