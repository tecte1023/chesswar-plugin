package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.service.BoardFactory;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Level;

@RequiredArgsConstructor
public class BoardService {
    private final BoardFactory factory;
    private final BoardRenderer renderer;

    public Board createAndRenderBoard(Player player) {
        Board board = factory.createBoard(player);

        renderer.render(board, player.getWorld());

        return board;
    }
}
