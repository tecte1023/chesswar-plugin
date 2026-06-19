package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

@Getter
@Accessors(fluent = true)
public class Barracks {
    private final Team team;
    private final ChessBoard board;
    private final Location spawnLocation;
    private final Location chestLocation1;
    private final Location chestLocation2;
    private final BlockFace chestFacing;

    public Barracks(Team team, ChessBoard mainBoard) {
        this.team = team;
        
        int cellSize = mainBoard.cellSize();
        int offsetDistance = 5 + (ChessFormation.BOARD_SIZE * cellSize);
        
        // 1. 배럭 보드 생성
        Location barracksOrigin;
        if (team == Team.WHITE) {
            barracksOrigin = mainBoard.origin().clone()
                    .subtract(mainBoard.forward().getDirection().multiply(offsetDistance));
        } else {
            barracksOrigin = mainBoard.origin().clone()
                    .add(mainBoard.forward().getDirection().multiply(offsetDistance));
        }
        this.board = ChessBoard.of(barracksOrigin, mainBoard.forward(), cellSize);

        // 2. 스폰 위치 캐싱
        Coordinate spawnCoord = (team == Team.WHITE) ? Coordinate.of(3, 6) : Coordinate.of(3, 1);
        this.spawnLocation = board.toCenterLocation(spawnCoord)
                .add(board.right().getDirection().multiply(cellSize / 2.0));
        
        if (team == Team.WHITE) {
            this.spawnLocation.setDirection(mainBoard.forward().getDirection().multiply(-1));
        } else {
            this.spawnLocation.setDirection(mainBoard.forward().getDirection());
        }

        // 3. 상자 위치 및 방향 캐싱
        int row = (team == Team.WHITE) ? 4 : 3;
        int dFileRight = 11;
        int eFileLeft = 12;
        int rankCenter = row * cellSize + 1;

        this.chestLocation1 = board.origin().clone()
                .add(board.right().getDirection().multiply(dFileRight))
                .add(board.forward().getDirection().multiply(rankCenter))
                .toBlockLocation();
        this.chestLocation2 = board.origin().clone()
                .add(board.right().getDirection().multiply(eFileLeft))
                .add(board.forward().getDirection().multiply(rankCenter))
                .toBlockLocation();
        
        this.chestFacing = (team == Team.WHITE) ? board.forward().getOppositeFace() : board.forward();
    }

    public boolean hasChest(final Location location) {
        if (location == null) {
            return false;
        }
        final Location blockLoc = location.toBlockLocation();
        return chestLocation1.equals(blockLoc) || chestLocation2.equals(blockLoc);
    }
}
