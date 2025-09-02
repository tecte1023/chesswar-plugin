package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;
import lombok.NonNull;
import org.bukkit.World;

public interface BoardRenderer {
    void render(@NonNull Board board, @NonNull World world);
}
