package dev.tecte.chessWar.infrastructure.persistence.exception;

import dev.tecte.chessWar.common.exception.Loggable;
import lombok.NonNull;

/**
 * 영속성 계층 초기화 중에 오류가 발생했을 때 던져지는 예외입니다.
 */
public class PersistenceInitializationException extends RuntimeException implements Loggable {
    private PersistenceInitializationException(@NonNull String message) {
        super(message);
    }

    private PersistenceInitializationException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }

    /**
     * 디렉토리 생성에 실패했을 때 이 예외를 생성합니다.
     *
     * @param path 생성에 실패한 디렉토리 경로
     * @return {@link PersistenceInitializationException}의 새 인스턴스
     */
    @NonNull
    public static PersistenceInitializationException forDirectoryCreationFailure(@NonNull String path) {
        return new PersistenceInitializationException("Failed to create parent directories for: " + path);
    }

    /**
     * 파일 생성에 실패했을 때 이 예외를 생성합니다.
     *
     * @param fileName 생성에 실패한 파일 이름
     * @param cause    예외의 원인
     * @return {@link PersistenceInitializationException}의 새 인스턴스
     */
    @NonNull
    public static PersistenceInitializationException forFileCreationFailure(
            @NonNull String fileName,
            @NonNull Throwable cause
    ) {
        return new PersistenceInitializationException("Could not create new file: " + fileName, cause);
    }
}
