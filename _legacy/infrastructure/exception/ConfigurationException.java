package dev.tecte.chessWar.infrastructure.exception;

import dev.tecte.chessWar.common.exception.SystemException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 설정 및 초기화 관련 시스템 예외입니다.
 * <p>
 * 이 예외는 로그에 상세 내용을 기록하며, 사용자에게는 별도의 알림을 보내지 않습니다.
 */
public class ConfigurationException extends SystemException {
    private ConfigurationException(@NonNull String internalMessage, @Nullable Throwable cause) {
        super(internalMessage, cause);
    }

    /**
     * 저장소 설정 실패 시 예외를 생성합니다.
     *
     * @param path  설정에 실패한 경로
     * @param cause 실패 원인
     * @return 생성된 예외
     */
    @NonNull
    public static ConfigurationException forStorageSetupFailure(@NonNull String path, @Nullable Throwable cause) {
        return new ConfigurationException("Failed to setup storage [Path: %s]".formatted(path), cause);
    }

    /**
     * 파일 생성 실패 시 예외를 생성합니다.
     *
     * @param fileName 생성을 시도한 파일 이름
     * @param cause    실패 원인
     * @return 생성된 예외
     */
    @NonNull
    public static ConfigurationException forCreationFailure(@NonNull String fileName, @Nullable Throwable cause) {
        return new ConfigurationException("Failed to create file [File: %s]".formatted(fileName), cause);
    }
}
