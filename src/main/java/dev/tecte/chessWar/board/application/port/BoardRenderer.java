package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface BoardRenderer {
    void render(@NotNull Board board, @NotNull World world);
}
