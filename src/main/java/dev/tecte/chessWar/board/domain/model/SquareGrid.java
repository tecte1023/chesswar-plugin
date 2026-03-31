package dev.tecte.chessWar.board.domain.model;

import dev.tecte.chessWar.board.domain.model.spec.GridSpec;
import dev.tecte.chessWar.board.domain.model.spec.SquareSpec;
import lombok.NonNull;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * 좌표 기반의 물리 영역 계산을 담당합니다.
 * 기준점으로부터 논리적 좌표를 물리적 영역으로 변환합니다.
 *
 * @param anchor      격자 기준점
 * @param orientation 격자 배치 방향
 * @param gridSpec    격자 크기 명세
 * @param squareSpec  개별 칸의 물리 크기 명세
 */
public record SquareGrid(
        Vector anchor,
        Orientation orientation,
        GridSpec gridSpec,
        SquareSpec squareSpec
) {
    public SquareGrid {
        Objects.requireNonNull(anchor, "Anchor cannot be null");
        Objects.requireNonNull(orientation, "Orientation cannot be null");
        Objects.requireNonNull(gridSpec, "GridSpec cannot be null");
        Objects.requireNonNull(squareSpec, "SquareSpec cannot be null");

        anchor = anchor.clone();
    }

    /**
     * 격자를 생성합니다.
     *
     * @param anchor      격자 기준점
     * @param orientation 격자 배치 방향
     * @param gridSpec    행/열 개수 명세
     * @param squareSpec  칸 물리 크기 명세
     * @return 격자
     */
    @NonNull
    public static SquareGrid of(
            @NonNull Vector anchor,
            @NonNull Orientation orientation,
            @NonNull GridSpec gridSpec,
            @NonNull SquareSpec squareSpec
    ) {
        return new SquareGrid(anchor, orientation, gridSpec, squareSpec);
    }

    /**
     * 해당 좌표의 물리적 칸을 산출합니다.
     *
     * @param coordinate 대상 좌표
     * @return 해당 좌표의 칸
     */
    @NonNull
    public Square squareAt(@NonNull Coordinate coordinate) {
        int row = coordinate.row();
        int col = coordinate.col();
        SquareColor color = (row + col) % 2 == 0 ? SquareColor.BLACK : SquareColor.WHITE;
        Vector squareOrigin = anchor()
                .add(colStep().multiply(col))
                .add(rowStep().multiply(row));
        Vector diagonalCorner = squareOrigin.clone()
                .add(colStep())
                .add(rowStep());

        return new Square(color, BoundingBox.of(squareOrigin, diagonalCorner));
    }

    /**
     * 해당 좌표의 칸 중심점을 제공합니다.
     *
     * @param coordinate 대상 좌표
     * @return 칸 중심점
     */
    @NonNull
    public Vector centerOf(@NonNull Coordinate coordinate) {
        return squareAt(coordinate).boundingBox().getCenter();
    }

    /**
     * 특정 방향으로 반 칸 이동하는 변위를 산출합니다.
     *
     * @param direction 이동 방향
     * @return 이동 변위
     */
    @NonNull
    public Vector halfStep(@NonNull Orientation direction) {
        double distance = (orientation.isParallelTo(direction) ? squareSpec.height() : squareSpec.width()) / 2.0;

        return direction.forward().multiply(distance);
    }

    /**
     * 격자 전체의 물리 영역을 산출합니다.
     *
     * @return 격자 전체 영역
     */
    @NonNull
    public BoundingBox boundingBox() {
        Vector diagonalOffset = colStep()
                .multiply(gridSpec.colCount())
                .add(rowStep().multiply(gridSpec.rowCount()));
        Vector diagonalCorner = anchor().add(diagonalOffset);

        return BoundingBox.of(anchor(), diagonalCorner);
    }

    /**
     * 열 방향 단위 이동 변위를 제공합니다.
     *
     * @return 열 이동 변위
     */
    @NonNull
    public Vector colStep() {
        return orientation.right().multiply(squareSpec.width());
    }

    /**
     * 행 방향 단위 이동 변위를 제공합니다.
     *
     * @return 행 이동 변위
     */
    @NonNull
    public Vector rowStep() {
        return orientation.forward().multiply(squareSpec.height());
    }

    /**
     * 격자의 기준점 정보를 제공합니다.
     *
     * @return 격자 기준점
     */
    @NonNull
    public Vector anchor() {
        return anchor.clone();
    }
}
