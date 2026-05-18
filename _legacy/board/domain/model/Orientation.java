package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 마인크래프트 월드 방위와 체스판 논리 좌표의 매핑을 정의합니다.
 */
public enum Orientation {
    NORTH(BlockFace.NORTH),
    EAST(BlockFace.EAST),
    SOUTH(BlockFace.SOUTH),
    WEST(BlockFace.WEST);

    private final Vector forward;
    private final Vector backward;
    private final Vector left;
    private final Vector right;

    private static final Map<String, Orientation> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Orientation::name, Function.identity()));

    Orientation(@NonNull BlockFace blockFace) {
        forward = blockFace.getDirection();
        backward = forward().multiply(-1);
        // 위쪽 벡터와 정면 벡터를 외적하면 정면을 기준으로 왼쪽을 가리키는 벡터가 나옴
        left = BlockFace.UP.getDirection().crossProduct(forward);
        right = left().multiply(-1);
    }

    /**
     * 상세 방위를 체스판 배치를 위한 4가지 기본 방향으로 변환합니다.
     *
     * @param blockFace 플레이어 시선 방향
     * @return 매핑된 체스판 방위
     */
    @NonNull
    public static Orientation from(@NonNull BlockFace blockFace) {
        return switch (blockFace) {
            case EAST, EAST_NORTH_EAST, EAST_SOUTH_EAST -> EAST;
            case SOUTH, SOUTH_EAST, SOUTH_WEST -> SOUTH;
            case WEST, WEST_NORTH_WEST, WEST_SOUTH_WEST -> WEST;
            default -> NORTH;
        };
    }

    /**
     * 이름 기반으로 방위를 검색합니다.
     *
     * @param name 방위 이름
     * @return 찾은 방위
     */
    @NonNull
    public static Optional<Orientation> from(@NonNull String name) {
        return Optional.ofNullable(LOOKUP.get(name.toUpperCase()));
    }

    /**
     * 현재 방위의 정반대 방향을 도출합니다.
     *
     * @return 반대편 방위
     */
    @NonNull
    public Orientation opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    /**
     * 대상 방위가 현재 방위와 평행한지 확인합니다.
     *
     * @param other 비교 대상
     * @return 평행 여부
     */
    public boolean isParallelTo(@NonNull Orientation other) {
        return this == other || this == other.opposite();
    }

    /**
     * 정면 방향을 제공합니다.
     *
     * @return 정면 방향
     */
    @NonNull
    public Vector forward() {
        return forward.clone();
    }

    /**
     * 후면 방향을 제공합니다.
     *
     * @return 후면 방향
     */
    @NonNull
    public Vector backward() {
        return backward.clone();
    }

    /**
     * 왼쪽 방향을 제공합니다.
     *
     * @return 왼쪽 방향
     */
    @NonNull
    public Vector left() {
        return left.clone();
    }

    /**
     * 오른쪽 방향을 제공합니다.
     *
     * @return 오른쪽 방향
     */
    @NonNull
    public Vector right() {
        return right.clone();
    }
}
