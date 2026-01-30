package dev.tecte.chessWar.infrastructure.persistence.exception;

import dev.tecte.chessWar.common.exception.SystemException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * YAML 데이터 매핑 관련 시스템 예외입니다.
 * <p>
 * 이 예외는 시스템 로그만 기록하며, 사용자에게는 별도의 알림을 보내지 않습니다.
 */
public class YmlMappingException extends SystemException {
    private YmlMappingException(@NonNull String internalMessage) {
        this(internalMessage, null);
    }

    private YmlMappingException(@NonNull String internalMessage, @Nullable Throwable cause) {
        super(internalMessage, cause);
    }

    /**
     * 키 누락 예외를 생성합니다.
     *
     * @param key  누락된 키
     * @param path 섹션 경로
     * @return 생성된 예외
     */
    @NonNull
    public static YmlMappingException forMissingKey(@NonNull String key, @Nullable String path) {
        return new YmlMappingException("Required key is missing [Key: %s, Section: %s]".formatted(key, path));
    }

    /**
     * 타입 불일치 예외를 생성합니다.
     *
     * @param key      키
     * @param path     섹션 경로
     * @param expected 기대 타입
     * @param actual   실제 타입
     * @return 생성된 예외
     */
    @NonNull
    public static YmlMappingException forInvalidType(
            @NonNull String key,
            @Nullable String path,
            @NonNull String expected,
            @NonNull String actual
    ) {
        return new YmlMappingException(
                "Invalid value type [Key: %s, Path: %s, Expected: %s, Actual: %s]".formatted(key, path, expected, actual)
        );
    }

    /**
     * Enum 상수 불일치 예외를 생성합니다.
     *
     * @param key   키
     * @param path  섹션 경로
     * @param value 잘못된 값
     * @return 생성된 예외
     */
    @NonNull
    public static YmlMappingException forInvalidEnumValue(
            @NonNull String key,
            @Nullable String path,
            @NonNull String value
    ) {
        return new YmlMappingException("Invalid enum value [Key: %s, Path: %s, Value: %s]".formatted(key, path, value));
    }

    /**
     * 형식 불일치 예외를 생성합니다.
     *
     * @param value          잘못된 값
     * @param path           섹션 경로
     * @param expectedFormat 기대 형식
     * @param cause          원인 예외
     * @return 생성된 예외
     */
    @NonNull
    public static YmlMappingException forInvalidFormat(
            @NonNull String value,
            @Nullable String path,
            @NonNull String expectedFormat,
            @Nullable Throwable cause
    ) {
        return new YmlMappingException(
                "Invalid value format [Value: %s, Section: %s, Expected: %s]".formatted(value, path, expectedFormat),
                cause
        );
    }

    /**
     * 필수 체스판 데이터 누락 예외를 생성합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static YmlMappingException forMissingBoard() {
        return new YmlMappingException("Required resource is missing [Resource: Board, Context: Game Creation]");
    }
}
