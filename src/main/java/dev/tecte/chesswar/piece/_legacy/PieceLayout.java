/*
package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.team.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class PieceLayout {
    public static final int KING_X = 4;
    public static final int QUEEN_X = 3;

    public static final int WHITE_BACK_RANK = 0;
    public static final int WHITE_PAWN_RANK = 1;
    public static final int BLACK_BACK_RANK = 7;
    public static final int BLACK_PAWN_RANK = 6;

    @NotNull
    public static PieceType getInitialPieceType(@NotNull final Coordinate coordinate) {
        final int x = coordinate.x();
        final int y = coordinate.y();

        if (y == WHITE_PAWN_RANK || y == BLACK_PAWN_RANK) {
            return PieceType.PAWN;
        }

        return switch (x) {
            case 0, 7 -> PieceType.ROOK;
            case 1, 6 -> PieceType.KNIGHT;
            case 2, 5 -> PieceType.BISHOP;
            case QUEEN_X -> PieceType.QUEEN;
            case KING_X -> PieceType.KING;
            default -> PieceType.PAWN;
        };
    }

    @NotNull
    public static Team getTeamAt(@NotNull final Coordinate coordinate) {
        return (coordinate.y() < 4) ? Team.WHITE : Team.BLACK;
    }

    @NotNull
    public static Map<Coordinate, PieceType> getFullInitialLayout() {
        final Map<Coordinate, PieceType> layout = new HashMap<>();
        for (final int y : new int[]{WHITE_BACK_RANK, WHITE_PAWN_RANK, BLACK_PAWN_RANK, BLACK_BACK_RANK}) {
            for (int x = 0; x < Coordinate.BOARD_SIZE; x++) {
                final Coordinate coord = Coordinate.of(x, y);
                layout.put(coord, getInitialPieceType(coord));
            }
        }
        return layout;
    }
}
*/
