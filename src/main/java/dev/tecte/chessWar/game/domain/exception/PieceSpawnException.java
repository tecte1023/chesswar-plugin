package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Loggable;
import dev.tecte.chessWar.common.exception.Notifiable;
import dev.tecte.chessWar.game.domain.model.PieceSpec;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

/**
 * 기물 스폰 과정에서 실패가 발생했을 때 던져지는 예외입니다.
 */
public class PieceSpawnException extends BusinessException implements Loggable, Notifiable {
    private final PieceSpec pieceSpec;

    private PieceSpawnException(@NonNull String message, @NonNull PieceSpec pieceSpec) {
        super(message);
        this.pieceSpec = pieceSpec;
    }

    /**
     * MythicMob 정의를 찾지 못했을 때 사용하는 정적 팩토리 메서드입니다.
     */
    @NonNull
    public static PieceSpawnException mobNotFound(@NonNull PieceSpec spec) {
        String message = String.format("Mythic mob definition not found for mob ID: '%s'.", spec.mobId());

        return new PieceSpawnException(message, spec);
    }

    /**
     * MythicMobs 스폰 결과가 null일 때 사용하는 정적 팩토리 메서드입니다.
     */
    @NonNull
    public static PieceSpawnException spawnResultNull(@NonNull PieceSpec spec) {
        String message = String.format("Failed to spawn MythicMob entity (spawn result was null) for mob ID: '%s'.",
                spec.mobId());

        return new PieceSpawnException(message, spec);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Component getNotificationComponent() {
        return Component.text("'")
                .append(pieceSpec.displayName())
                .append(Component.text("' 기물 소환에 실패했습니다."));
    }
}
