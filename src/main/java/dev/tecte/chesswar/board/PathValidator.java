package dev.tecte.chesswar.board;

import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;

/**
 * [Manager] 경로 내 장애물 충돌을 검사하는 정적 공간 연산 유틸리티.
 *
 * <p>이동 경로 선상의 기물 존재 여부를 검사하는 책임을 가지며,
 * {@code SlidingPolicy}, {@code PawnPolicy} 등 여러 이동 정책에서 공통으로 참조한다.
 * 직접 상태를 소유하지 않으며 모든 메서드는 정적(static)이다.
 */
public final class PathValidator {

    private PathValidator() {
    }

    /**
     * 두 좌표 사이의 경로가 이동 가능한지 검사한다.
     *
     * <p>경로 중간에 아군 기물이 없어야 하며, 적 기물은 최대 1기까지만 허용한다.
     * {@code 도약} 버프를 보유한 경우, 아군/적 기물 1기를 뛰어넘는 것을 허용한다.
     *
     * @param state  현재 보드 전역 기물 배치 상태
     * @param from   출발 좌표
     * @param to     목적지 좌표
     * @param piece  이동하는 기물
     * @return 경로가 통행 가능하면 {@code true}
     */
    public static boolean isClear(
            final PieceState state,
            final Coordinate from,
            final Coordinate to,
            final Piece piece
    ) {
        final int xDirection = Integer.compare(to.x(), from.x());
        final int yDirection = Integer.compare(to.y(), from.y());
        final int steps = Math.max(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y()));

        int obstacleCount = 0;

        for (int i = 1; i < steps; i++) {
            final int currentX = from.x() + (xDirection * i);
            final int currentY = from.y() + (yDirection * i);
            final Coordinate coord = Coordinate.of(currentX, currentY);
            final Piece obstacle = state.piece(coord);

            if (obstacle == null) {
                continue;
            }

            if (obstacle.team() != piece.team()) {
                return false;
            }

            obstacleCount++;
        }

        if (obstacleCount == 0) {
            return true;
        }

        return piece.hasEffect("도약") && obstacleCount == 1;
    }
}
