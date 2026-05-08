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
     * @param spec  기물 명세
     * @param cause 실패 원인
     * @return 생성된 예외
     */
    @NonNull
    public static PieceSystemException spawnFailed(@NonNull PieceSpec spec, @NonNull Throwable cause) {
        return new PieceSystemException(
                "Failed to spawn piece [Type: %s, Team: %s]".formatted(spec.type(), spec.teamColor()),
                "시스템 오류로 %s 소환에 실패했습니다.".formatted(spec.name()),
                cause
        );
    }

    /**
     * 데이터 불일치로 기물 엔티티를 찾을 수 없을 때 발생합니다.
     *
     * @param entityId 엔티티 ID
     * @return 생성된 예외
     */
    @NonNull
    public static PieceSystemException pieceEntityMissing(@NonNull UUID entityId) {
        return new PieceSystemException(
                "Actual entity is missing for UnitPiece [EntityID: %s]".formatted(entityId),
                "시스템 오류로 기물 정보를 가져오는 데 실패했습니다."
        );
    }

    /**
     * 기물 ID 형식이 올바르지 않을 때 발생합니다.
     *
     * @param pieceId 입력된 ID
     * @param cause   실패 원인
     * @return 생성된 예외
     */
    @NonNull
    public static PieceSystemException invalidId(@NonNull String pieceId, @NonNull Throwable cause) {
        return new PieceSystemException(
                "Invalid Piece ID format provided [Input: %s]".formatted(pieceId),
                "시스템 오류로 기물을 선택하는 데 실패했습니다.",
                cause
        );
    }
}
