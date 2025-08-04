package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.board.domain.model.Board;
import org.bukkit.World;

public interface BoardRenderer {
    void render(Board board, World world);
}
