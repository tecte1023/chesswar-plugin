package dev.tecte.chessWar.infrastructure.persistence.exception;

import dev.tecte.chessWar.common.exception.SystemException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 데이터 영속성 관련 시스템 예외입니다.
 * <p>
 * 이 예외는 시스템 로그만 기록하며, 사용자에게는 별도의 알림을 보내지 않습니다.
 */
public class PersistenceException extends SystemException {
    private PersistenceException(@NonNull String internalMessage, @Nullable Throwable cause) {
        super(internalMessage, cause);
    }

    /**
     * 데이터 저장 실패 시 예외를 생성합니다.
     *
     * @param targetName 저장 대상 명칭
     * @param cause      실패 원인
     * @return 생성된 예외
     */
    @NonNull
    public static PersistenceException forSaveFailure(@NonNull String targetName, @Nullable Throwable cause) {
        return new PersistenceException("Failed to save [Target: %s]".formatted(targetName), cause);
    }
}
