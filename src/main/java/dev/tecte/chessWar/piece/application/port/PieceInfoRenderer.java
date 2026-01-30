package dev.tecte.chessWar.piece.application.port;

import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 기물 정보를 화면에 표시합니다.
 */
public interface PieceInfoRenderer {
    /**
     * 기물 정보를 플레이어에게 표시합니다.
     *
     * @param player     표시 대상 플레이어
     * @param piece      표시할 기물
     * @param isSelected 기물의 선택 여부
     */
    void renderInfo(@NonNull Player player, @NonNull UnitPiece piece, boolean isSelected);
}
