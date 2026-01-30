package dev.tecte.chessWar.piece.domain.exception;

import dev.tecte.chessWar.common.exception.NotifiableSystemException;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 기물 도메인에서 발생하는 사용자 알림이 필요한 시스템 예외입니다.
 * <p>
 * 이 예외는 시스템 로그를 기록하고, 사용자에게도 알림 메시지를 전달합니다.
 */
public class PieceSystemException extends NotifiableSystemException {
    private PieceSystemException(@NonNull String internalMessage, @NonNull String userMessage) {
        this(internalMessage, userMessage, null);
    }

    private PieceSystemException(
            @NonNull String internalMessage,
            @NonNull String userMessage,
            @Nullable Throwable cause
    ) {
        super(internalMessage, userMessage, cause);
    }

    /**
     * 기물 소환에 실패했을 때 발생합니다.
     *
     * @param spec  소환을 시도한 기물의 명세
     * @param cause 실패 원인
     * @return 생성된 예외
     */
    @NonNull
    public static PieceSystemException spawnFailed(@NonNull PieceSpec spec, @NonNull Throwable cause) {
        return new PieceSystemException(
                "Failed to spawn piece [Type: %s, Team: %s]".formatted(spec.type(), spec.teamColor()),
                "시스템 오류로 " + spec.name() + " 소환에 실패했습니다.",
                cause
        );
    }

    /**
     * 데이터 불일치로 인해 엔티티를 찾을 수 없을 때 발생합니다.
     *
     * @param entityId 찾을 수 없는 엔티티의 식별자
     * @return 생성된 예외
     */
    @NonNull
    public static PieceSystemException entityMissing(@NonNull UUID entityId) {
        return new PieceSystemException(
                "UnitPiece exists in domain memory but actual entity is missing [EntityID: %s]".formatted(entityId),
                "시스템 오류로 기물 정보를 가져오는 데 실패했습니다."
        );
    }
}
