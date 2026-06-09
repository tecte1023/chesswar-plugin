package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(staticName = "create")
public class BoardState {
    private ChessBoard currentBoard;
    private final Map<Team, Barracks> barracksMap = new HashMap<>();
    private final Set<Location> barracksChests = new HashSet<>();
    private final Map<Location, Team> chestTeamOwnership = new HashMap<>();

    public void currentBoard(final ChessBoard board) {
        currentBoard = board;
    }

    public void reset() {
        currentBoard = null;
        barracksMap.clear();
        barracksChests.clear();
        chestTeamOwnership.clear();
    }
}
