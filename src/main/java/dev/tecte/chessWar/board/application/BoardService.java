package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.board.domain.service.BoardFactory;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class BoardService {
    private final BoardFactory factory;
    private final BoardRenderer renderer;

    public void createBoard(Player player) {
        renderer.render(factory.createBoard(player), player.getWorld());
    }
}
