package dev.tecte.chessWar.board.domain.model;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * 체스판의 격자 영역을 계산하고 관리하는 클래스입니다.
 * 격자의 기준점(a1), 방향, 크기 정보를 바탕으로 각 칸의 위치와 영역을 계산합니다.
 */
@Getter
public final class SquareGrid {
    private final Vector anchor;
    private final Orientation orientation;
    private final int rowCount;
    private final int colCount;
    private final int squareWidth;
    private final int squareHeight;
    private final Vector colStep;
    private final Vector rowStep;

    private SquareGrid(
            @NonNull Vector anchor,
            @NonNull Orientation orientation,
            int rowCount,
            int colCount,
            int squareWidth,
            int squareHeight
    ) {
        this.anchor = anchor;
        this.orientation = orientation;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.squareWidth = squareWidth;
        this.squareHeight = squareHeight;
        // 반복적으로 계산하는 것을 피하고, 각 칸의 위치를 효율적으로 찾을 수 있게 colStep과 rowStep을 미리 계산
        colStep = orientation.getRight().clone().multiply(squareWidth);
        rowStep = orientation.getForward().clone().multiply(squareHeight);
    }

    /**
     * {@link SquareGrid}의 새 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param anchor       격자의 기준점 (a1: 좌측 하단 모서리)
     * @param orientation  격자의 방향
     * @param rowCount     격자의 행 수
     * @param colCount     격자의 열 수
     * @param squareWidth  각 칸의 너비
     * @param squareHeight 각 칸의 높이
     * @return 생성된 {@link SquareGrid} 인스턴스
     */
    @NonNull
    public static SquareGrid create(
            @NonNull Vector anchor,
            @NonNull Orientation orientation,
            int rowCount,
            int colCount,
            int squareWidth,
            int squareHeight
    ) {
        return new SquareGrid(
                anchor,
                orientation,
                rowCount,
                colCount,
                squareWidth,
                squareHeight
        );
    }

    /**
     * 격자 전체의 영역을 계산합니다.
     *
     * @return 격자의 영역
     */
    @NonNull
    public BoundingBox getBoundingBox() {
        Vector diagonalOffset = colStep.clone().multiply(colCount)
                .add(rowStep.clone().multiply(rowCount));
        Vector diagonalCorner = anchor.clone().add(diagonalOffset);

        return BoundingBox.of(anchor, diagonalCorner);
    }

    /**
     * 지정된 행과 열에 위치한 체스판 칸({@link Square})의 정보를 계산합니다.
     *
     * @param row 칸의 행 인덱스 (0부터 시작)
     * @param col 칸의 열 인덱스 (0부터 시작)
     * @return 계산된 {@link Square} 객체
     */
    @NonNull
    public Square getSquareAt(int row, int col) {
        Vector squareOrigin = anchor.clone()
                .add(colStep.clone().multiply(col))
                .add(rowStep.clone().multiply(row));
        Vector diagonalCorner = squareOrigin.clone().add(colStep).add(rowStep);
        BoundingBox boundingBox = BoundingBox.of(squareOrigin, diagonalCorner);
        SquareColor color = (row + col) % 2 == 0 ? SquareColor.BLACK : SquareColor.WHITE;

        return new Square(boundingBox, color);
    }
}
