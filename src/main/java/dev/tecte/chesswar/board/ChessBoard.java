package dev.tecte.chesswar.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

@Getter
@Accessors(fluent = true)
public class ChessBoard {
    private final Location origin;
    private final BlockFace forward;
    private final BlockFace right;
    private final Vector forwardVector;
    private final Vector rightVector;
    private final int cellSize;

    public ChessBoard(final Location origin, final BlockFace forward, final int cellSize) {
        // 기준점을 블록의 정중앙(x.5, z.5)으로 설정하여 좌표계 비대칭성 제거
        final Location blockLoc = origin.getBlock().getLocation();
        blockLoc.add(0.5, 0.0, 0.5);
        this.origin = blockLoc;

        this.forward = forward;
        this.right = getRightFace(forward);
        this.forwardVector = forward.getDirection();
        this.rightVector = right.getDirection();
        this.cellSize = cellSize;
    }

    public Location toLocation(final Coordinate coordinate) {
        return updateToLocation(coordinate, origin.clone());
    }

    public Location toCenterLocation(final Coordinate coordinate) {
        return updateToCenterLocation(coordinate, origin.clone());
    }

    public Location updateToLocation(final Coordinate coordinate, final Location target) {
        // origin이 정중앙(0.5)이므로, 좌하단 모서리를 기준으로 계산하기 위해 -0.5 보정
        final double offsetX = (rightVector.getX() * (coordinate.x() * cellSize)) + (forwardVector.getX() * (coordinate.y() * cellSize));
        final double offsetZ = (rightVector.getZ() * (coordinate.x() * cellSize)) + (forwardVector.getZ() * (coordinate.y() * cellSize));

        target.setX(origin.getX() - 0.5 + offsetX);
        target.setY(origin.getY());
        target.setZ(origin.getZ() - 0.5 + offsetZ);

        return target;
    }

    public Location updateToCenterLocation(final Coordinate coordinate, final Location target) {
        // origin 자체가 블록 정중앙(0.5)을 포함하므로 순수 오프셋 계산만 수행
        // 3x3일 경우 1블록(halfCell=1)만 더하면 정중앙 블록의 중앙에 안착함
        final double halfCell = Math.floor(cellSize / 2.0);

        final double offsetX = (rightVector.getX() * (coordinate.x() * cellSize + halfCell)) + (forwardVector.getX() * (coordinate.y() * cellSize + halfCell));
        final double offsetZ = (rightVector.getZ() * (coordinate.x() * cellSize + halfCell)) + (forwardVector.getZ() * (coordinate.y() * cellSize + halfCell));

        target.setX(origin.getX() + offsetX);
        target.setY(origin.getY() + 0.1); // Visual offset for guides/particles
        target.setZ(origin.getZ() + offsetZ);

        return target;
    }

    public Coordinate toCoordinate(final Location location) {
        final int blockOffsetX = location.getBlockX() - origin.getBlockX();
        final int blockOffsetZ = location.getBlockZ() - origin.getBlockZ();
        final int forwardDot = blockOffsetX * forward.getModX() + blockOffsetZ * forward.getModZ();
        final int rightDot = blockOffsetX * right.getModX() + blockOffsetZ * right.getModZ();
        final int y = Math.floorDiv(forwardDot, cellSize);
        final int x = Math.floorDiv(rightDot, cellSize);

        return Coordinate.of(x, y);
    }

    private BlockFace getRightFace(final BlockFace forward) {
        return switch (forward) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case EAST -> BlockFace.SOUTH;
            case WEST -> BlockFace.NORTH;
            default -> throw new IllegalArgumentException("지원하지 않는 방향입니다: " + forward);
        };
    }
}
