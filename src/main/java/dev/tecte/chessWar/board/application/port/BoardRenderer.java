package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;
import lombok.NonNull;
import org.bukkit.World;

/**
 * 체스판을 렌더링합니다.
 */
public interface BoardRenderer {
    /**
     * 체스판을 월드에 렌더링합니다.
     *
     * @param board 렌더링할 체스판
     * @param world 렌더링할 월드
     */
    void render(@NonNull Board board, @NonNull World world);
}
