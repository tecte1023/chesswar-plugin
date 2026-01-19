package dev.tecte.chessWar.infrastructure.persistence.exception;

import dev.tecte.chessWar.common.exception.SystemException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 데이터 영속성 처리 중 발생하는 시스템 예외입니다.
 * <p>
 * 파일 입출력 오류, 데이터 변환 실패 등의 상황에서 발생합니다.
 * 이 예외는 기본적으로 사용자에게 알림을 보내지 않고 로그만 기록합니다.
 */
public class PersistenceException extends SystemException {
    private PersistenceException(@NonNull String internalMessage, @Nullable Throwable cause) {
        super(internalMessage, cause);
    }

    /**
     * 데이터 저장 실패 시 예외를 생성합니다.
     *
     * @param targetName 저장 대상 명칭
     * @param cause      원인 예외
     * @return {@link PersistenceException}의 새 인스턴스
     */
    @NonNull
    public static PersistenceException forSaveFailure(@NonNull String targetName, @Nullable Throwable cause) {
        return new PersistenceException("Failed to save [Target: %s]".formatted(targetName), cause);
    }
}
