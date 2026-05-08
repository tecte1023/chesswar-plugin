package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardCreationParams;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.BorderType;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.board.domain.model.spec.BorderSpec;
import dev.tecte.chessWar.board.domain.model.spec.GridSpec;
import dev.tecte.chessWar.board.domain.model.spec.SquareSpec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.util.Vector;

/**
 * 체스판의 생성과 조립을 담당합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardFactory {
    private final GridSpec gridSpec;
    private final SquareSpec squareSpec;
    private final BorderSpec borderSpec;

    /**
     * 특정 위치와 방향을 기반으로 체스판을 생성합니다.
     *
     * @param worldName      생성될 월드 이름
     * @param playerPosition 플레이어 위치
     * @param orientation    플레이어 방향
     * @return 생성된 체스판
     */
    @NonNull
    public Board createAt(
            @NonNull String worldName,
            @NonNull Vector playerPosition,
            @NonNull Orientation orientation
    ) {
        Vector gridAnchor = determineAnchor(playerPosition, orientation);

        return create(BoardCreationParams.of(worldName, gridAnchor, orientation));
    }

    /**
     * 파라미터를 기반으로 체스판을 조립합니다.
     *
     * @param params 생성 파라미터
     * @return 조립된 체스판
     */
    @NonNull
    public Board create(@NonNull BoardCreationParams params) {
        SquareGrid squareGrid = SquareGrid.of(
                params.gridAnchor(),
                params.orientation(),
                gridSpec,
                squareSpec
        );
        Border innerBorder = Border.from(
                BorderType.INNER_BORDER,
                squareGrid.boundingBox(),
                borderSpec.innerThickness()
        );
        Border frame = Border.from(
                BorderType.FRAME,
                innerBorder.boundingBox(),
                borderSpec.frameThickness()
        );

        return Board.of(params.worldName(), squareGrid, innerBorder, frame);
    }

    /**
     * 플레이어 위치를 기준으로 체스판의 기준점을 산출합니다.
     *
     * @param playerPosition 플레이어 위치
     * @param orientation    방향
     * @return 격자 기준점
     */
    private Vector determineAnchor(Vector playerPosition, Orientation orientation) {
        // 기준점: a1 칸 좌측 하단 모서리
        // 플레이어 정면 중앙 배치를 위해 격자 너비 절반만큼 왼쪽, 테두리 두께만큼 앞쪽으로 이동
        int gridWidth = squareSpec.width() * gridSpec.colCount();
        double totalThickness = borderSpec.totalThickness();
        Vector offset = orientation.left()
                .multiply(gridWidth / 2.0)
                .add(orientation.forward().multiply(totalThickness));

        return playerPosition.clone().add(offset);
    }
}
