package dev.tecte.chessWar.piece.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Loggable;
import dev.tecte.chessWar.common.exception.Notifiable;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * 기물 스폰 과정에서 실패가 발생했을 때 던져지는 예외입니다.
 */
public class PieceSpawnException extends BusinessException implements Loggable, Notifiable {
    private final PieceSpec pieceSpec;

    private PieceSpawnException(@NonNull String message, @NonNull PieceSpec pieceSpec) {
        super(message);
        this.pieceSpec = pieceSpec;
    }

    @NonNull
    @Override
    public Component getNotificationComponent() {
        return Component.text()
                .append(pieceSpec.displayName())
                .append(Component.text(" 기물 소환에 실패했습니다.", NamedTextColor.RED))
                .build();
    }
}
