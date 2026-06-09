package dev.tecte.chesswar.board;

import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;

import java.util.HashMap;
import java.util.Map;

public class ChessFormation {
    public static final int BOARD_SIZE = 8;
    public static final int KING_X = 4;
    public static final int QUEEN_X = 3;

    public static final int WHITE_BACK_RANK = 0;
    public static final int WHITE_PAWN_RANK = 1;
    public static final int BLACK_BACK_RANK = 7;
    public static final int BLACK_PAWN_RANK = 6;

    public static int getCampRankOffset(final Team team) {
        return (team == Team.WHITE) ? -5 : 12;
    }

    public static PieceType getInitialPieceType(final Coordinate coordinate) {
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

    public static Coordinate getKingCoordinate(final Team team) {
        return (team == Team.WHITE) 
                ? Coordinate.of(KING_X, WHITE_BACK_RANK) 
                : Coordinate.of(KING_X, BLACK_BACK_RANK);
    }

    public static Team getTeamAt(final Coordinate coordinate) {
        return (coordinate.y() < 4) ? Team.WHITE : Team.BLACK;
    }

    public static Map<Coordinate, PieceType> getFullInitialLayout() {
        final Map<Coordinate, PieceType> layout = new HashMap<>();
        for (final int y : new int[]{WHITE_BACK_RANK, WHITE_PAWN_RANK, BLACK_PAWN_RANK, BLACK_BACK_RANK}) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                final Coordinate coord = Coordinate.of(x, y);
                layout.put(coord, getInitialPieceType(coord));
            }
        }
        return layout;
    }
}
