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
        this.origin = origin.getBlock().getLocation();
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
        final double offsetX = (rightVector.getX() * (coordinate.x() * cellSize)) + (forwardVector.getX() * (coordinate.y() * cellSize));
        final double offsetZ = (rightVector.getZ() * (coordinate.x() * cellSize)) + (forwardVector.getZ() * (coordinate.y() * cellSize));

        target.setX(origin.getX() + offsetX);
        target.setY(origin.getY());
        target.setZ(origin.getZ() + offsetZ);

        return target;
    }

    public Location updateToCenterLocation(final Coordinate coordinate, final Location target) {
        final double halfCell = cellSize / 2.0;

        final double offsetX = (rightVector.getX() * (coordinate.x() * cellSize + halfCell)) + (forwardVector.getX() * (coordinate.y() * cellSize + halfCell));
        final double offsetZ = (rightVector.getZ() * (coordinate.x() * cellSize + halfCell)) + (forwardVector.getZ() * (coordinate.y() * cellSize + halfCell));

        // 마인크래프트 좌표계 특성상 음수 방향 벡터 사용 시 1블록 오차 보정 (+1.0)
        final double correctionX = (rightVector.getX() < 0 || forwardVector.getX() < 0) ? 1.0 : 0.0;
        final double correctionZ = (rightVector.getZ() < 0 || forwardVector.getZ() < 0) ? 1.0 : 0.0;

        target.setX(origin.getX() + offsetX + correctionX);
        target.setY(origin.getY() + 0.1); // Visual offset for guides/particles
        target.setZ(origin.getZ() + offsetZ + correctionZ);

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
