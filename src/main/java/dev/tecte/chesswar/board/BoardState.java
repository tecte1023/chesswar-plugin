package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter(onParam_ = {@NotNull})
@Accessors(fluent = true)
@NoArgsConstructor(staticName = "create")
public class BoardState {
    private final Map<Team, Barracks> barracksMap = new HashMap<>();

    @Nullable
    private ChessBoard currentBoard;
}
