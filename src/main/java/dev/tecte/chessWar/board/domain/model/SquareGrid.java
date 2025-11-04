package dev.tecte.chessWar.board.domain.model;

import dev.tecte.chessWar.board.domain.model.spec.GridSpec;
import dev.tecte.chessWar.board.domain.model.spec.SquareSpec;
import lombok.Builder;
import lombok.NonNull;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * 체스판의 격자 영역을 계산하고 관리하는 클래스입니다.
 * 격자의 기준점(a1), 방향, 크기 정보를 바탕으로 각 칸의 위치와 영역을 계산합니다.
 *
 * @param anchor      격자의 기준점 (a1: 좌측 하단 모서리)
 * @param orientation 격자의 방향
 * @param gridSpec    격자 명세
 * @param squareSpec  칸 명세
 */
@Builder
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
     * 격자의 기준점(a1) 벡터를 복제하여 반환합니다.
     *
     * @return 기준점 벡터의 복제본
     */
    @NonNull
    public Vector anchor() {
        return anchor.clone();
    }

    /**
     * 한 열을 오른쪽으로 이동하는 데 필요한 변위 벡터를 계산합니다.
     *
     * @return 열 이동 벡터
     */
    @NonNull
    public Vector colStep() {
        return orientation.right().multiply(squareSpec.width());
    }

    /**
     * 한 행을 앞으로 이동하는 데 필요한 변위 벡터를 계산합니다.
     *
     * @return 행 이동 벡터
     */
    @NonNull
    public Vector rowStep() {
        return orientation.forward().multiply(squareSpec.height());
    }

    /**
     * 격자 전체의 영역을 계산합니다.
     *
     * @return 격자의 영역
     */
    @NonNull
    public BoundingBox boundingBox() {
        Vector diagonalOffset = colStep().multiply(gridSpec.colCount())
                .add(rowStep().multiply(gridSpec.rowCount()));
        Vector diagonalCorner = anchor().add(diagonalOffset);

        return BoundingBox.of(anchor(), diagonalCorner);
    }

    /**
     * 지정된 좌표에 위치한 체스판 칸({@link Square})의 정보를 계산합니다.
     *
     * @param coordinate 칸의 좌표
     * @return 계산된 {@link Square} 객체
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
        BoundingBox boundingBox = BoundingBox.of(squareOrigin, diagonalCorner);

        return new Square(color, boundingBox);
    }
}
