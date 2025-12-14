package dev.tecte.chessWar.game.application.port;

import dev.tecte.chessWar.game.domain.model.Piece;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 기물의 상세 정보를 플레이어에게 시각적으로 보여주는 렌더러입니다.
 */
public interface PieceInfoRenderer {
    /**
     * 특정 기물의 정보를 플레이어에게 보여줍니다.
     *
     * @param player 정보를 볼 플레이어
     * @param piece  정보를 표시할 기물
     */
    void renderInfo(@NonNull Player player, @NonNull Piece piece);
}
