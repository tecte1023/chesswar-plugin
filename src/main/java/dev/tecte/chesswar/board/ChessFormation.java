package dev.tecte.chesswar.board;

import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;

/**
 * 체스판의 기물 배치 및 초기 상태를 정의하는 유틸리티 클래스입니다.
 */
public class ChessFormation {
    public static final int BOARD_SIZE = 8;
    public static final int KING_X = 4;
    public static final int QUEEN_X = 3;

    public static final int WHITE_BACK_RANK = 0;
    public static final int WHITE_PAWN_RANK = 1;
    public static final int BLACK_BACK_RANK = 7;
    public static final int BLACK_PAWN_RANK = 6;

    /**
     * 특정 좌표에 배치되어야 할 기본 기물 유형을 반환합니다.
     */
    public static PieceType getInitialPieceType(Coordinate coordinate) {
        int x = coordinate.x();
        int y = coordinate.y();

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

    /**
     * 각 팀의 킹이 위치해야 할 초기 좌표를 반환합니다.
     */
    public static Coordinate getKingCoordinate(Team team) {
        return (team == Team.WHITE) 
                ? Coordinate.of(KING_X, WHITE_BACK_RANK) 
                : Coordinate.of(KING_X, BLACK_BACK_RANK);
    }
}
