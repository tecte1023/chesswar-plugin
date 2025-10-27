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
 * 체스판이 놓일 방향을 나타내는 열거형입니다.
 * Bukkit의 8방향 {@link BlockFace}를 동서남북 4방향으로 단순화하여 사용합니다.
 */
public enum Orientation {
    NORTH(BlockFace.NORTH),
    EAST(BlockFace.EAST),
    SOUTH(BlockFace.SOUTH),
    WEST(BlockFace.WEST);

    private static final Map<String, Orientation> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Orientation::name, Function.identity()));

    private final Vector forward;
    private final Vector backward;
    private final Vector left;
    private final Vector right;

    Orientation(@NonNull BlockFace blockFace) {
        forward = blockFace.getDirection();
        backward = forward().multiply(-1);
        // 위쪽 벡터와 정면 벡터를 외적하면 항상 정면을 기준으로 왼쪽을 가리키는 벡터가 나옴
        left = BlockFace.UP.getDirection().crossProduct(forward);
        right = left().multiply(-1);
    }

    /**
     * Bukkit의 상세한 {@link BlockFace}를 4가지 기본 방향 중 하나로 변환합니다.
     *
     * @param blockFace 플레이어가 바라보는 방향
     * @return 4가지 기본 방향 중 하나
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
     * 문자열 값으로부터 {@link Orientation}을 찾습니다.
     *
     * @param value 방향을 나타내는 문자열 (대소문자 구분 없음)
     * @return 해당 방향의 {@link Orientation}을 담은 {@link Optional}, 없으면 빈 {@link Optional}
     */
    @NonNull
    public static Optional<Orientation> from(@NonNull String value) {
        return Optional.ofNullable(LOOKUP.get(value.toUpperCase()));
    }

    /**
     * 이 방향의 정면을 향하는 단위 벡터를 반환합니다.
     *
     * @return 정면 방향 벡터의 복제본
     */
    @NonNull
    public Vector forward() {
        return forward.clone();
    }

    /**
     * 이 방향의 후면을 향하는 단위 벡터를 반환합니다.
     *
     * @return 후면 방향 벡터의 복제본
     */
    @NonNull
    public Vector backward() {
        return backward.clone();
    }

    /**
     * 이 방향의 왼쪽을 향하는 단위 벡터를 반환합니다.
     *
     * @return 왼쪽 방향 벡터의 복제본
     */
    @NonNull
    public Vector left() {
        return left.clone();
    }

    /**
     * 이 방향의 오른쪽을 향하는 단위 벡터를 반환합니다.
     *
     * @return 오른쪽 방향 벡터의 복제본
     */
    @NonNull
    public Vector right() {
        return right.clone();
    }
}
