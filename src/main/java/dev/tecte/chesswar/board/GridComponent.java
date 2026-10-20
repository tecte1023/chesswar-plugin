package dev.tecte.chesswar.board;

import dev.tecte.chesswar.core.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class GridComponent implements Component {
    private GridPoint gridPos;
}
