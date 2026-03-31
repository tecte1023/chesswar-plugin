package dev.tecte.chessWar.board.domain.model;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * 체스판의 물리 구조와 공간 정보를 관리합니다.
 * 공간 구성 요소를 통합하고 위치 관련 비즈니스 규칙을 처리합니다.
 *
 * @param worldName   체스판이 위치한 월드 이름
 * @param squareGrid  격자 좌표 체계
 * @param innerBorder 내부 구획 테두리
 * @param frame       최외곽 프레임 테두리
 */
public record Board(
        String worldName,
        SquareGrid squareGrid,
        Border innerBorder,
        Border frame
) {
    public Board {
        Objects.requireNonNull(worldName, "World name cannot be null");
        Objects.requireNonNull(squareGrid, "Square grid cannot be null");
        Objects.requireNonNull(innerBorder, "Inner border cannot be null");
        Objects.requireNonNull(frame, "Frame cannot be null");
    }

    /**
     * 모든 구성 요소를 갖춘 체스판을 생성합니다.
     *
     * @param worldName   체스판이 위치할 월드 이름
     * @param squareGrid  격자 좌표 정보
     * @param innerBorder 내부 구획 테두리
     * @param frame       외부 프레임 테두리
     * @return 체스판
     */
    @NonNull
    public static Board of(
            @NonNull String worldName,
            @NonNull SquareGrid squareGrid,
            @NonNull Border innerBorder,
            @NonNull Border frame
    ) {
        return new Board(worldName, squareGrid, innerBorder, frame);
    }

    /**
     * 특정 좌표의 기물 소환 위치를 산출합니다.
     *
     * @param coordinate 기물 배치 좌표
     * @return 소환 위치
     */
    @NonNull
    public Vector spawnPositionOf(@NonNull Coordinate coordinate) {
        return squareGrid.centerOf(coordinate).add(new Vector(0, 1, 0));
    }

    /**
     * 팀별 진영 방향에 따른 플레이어 시작 위치를 산출합니다.
     *
     * @param teamColor 대상 팀 색상
     * @return 시작 위치
     */
    @NonNull
    public Vector startingPositionOf(@NonNull TeamColor teamColor) {
        Orientation startDirection = (teamColor == TeamColor.WHITE)
                ? squareGrid.orientation().opposite()
                : squareGrid.orientation();
        Vector offset = squareGrid.halfStep(startDirection);

        return center()
                .add(new Vector(0, 1, 0))
                .add(offset);
    }

    /**
     * 체스판 영역의 정중앙 지점을 산출합니다.
     *
     * @return 체스판 중심점
     */
    @NonNull
    public Vector center() {
        return squareGrid.boundingBox().getCenter();
    }

    /**
     * 체스판의 정면 방향을 제공합니다.
     *
     * @return 정면 방향
     */
    @NonNull
    public Vector forwardFacing() {
        return squareGrid.orientation().forward();
    }

    /**
     * 체스판의 후면 방향을 제공합니다.
     *
     * @return 후면 방향
     */
    @NonNull
    public Vector backwardFacing() {
        return squareGrid.orientation().backward();
    }
}
