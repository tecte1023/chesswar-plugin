package dev.tecte.chessWar.board.domain.model;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * 체스판이 놓일 방향을 나타내는 열거형입니다.
 * Bukkit의 8방향 {@link BlockFace}를 동서남북 4방향으로 단순화하여 사용합니다.
 */
@Getter
public enum Orientation {
    NORTH(BlockFace.NORTH),
    EAST(BlockFace.EAST),
    SOUTH(BlockFace.SOUTH),
    WEST(BlockFace.WEST);

    private final Vector forward;
    private final Vector backward;
    private final Vector left;
    private final Vector right;

    Orientation(@NonNull BlockFace blockFace) {
        forward = blockFace.getDirection();
        backward = forward.clone().multiply(-1);
        // 위쪽 벡터와 정면 벡터를 외적하면 항상 정면을 기준으로 왼쪽을 가리키는 벡터가 나옴
        left = BlockFace.UP.getDirection().crossProduct(forward);
        right = left.clone().multiply(-1);
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
}
