package dev.tecte.chessWar.infrastructure.persistence.exception;

import dev.tecte.chessWar.common.exception.Loggable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * YML 데이터 매핑 과정에서 발생하는 예외를 나타냅니다.
 * 데이터 파일의 값이 없거나, 타입이 유효하지 않은 경우 발생합니다.
 */
public class YmlMappingException extends RuntimeException implements Loggable {
    private YmlMappingException(@NonNull String message) {
        super(message);
    }

    /**
     * 필수 키가 없을 때 예외를 생성합니다.
     *
     * @param key  누락된 키
     * @param path 탐색한 경로
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forMissingKey(@NonNull String key, @Nullable String path) {
        return new YmlMappingException(String.format("Required key '%s' is missing in section '%s'.", key, path));
    }

    /**
     * 값의 타입이 유효하지 않을 때 예외를 생성합니다.
     *
     * @param key      키
     * @param path     탐색한 경로
     * @param expected 기대한 타입
     * @param actual   실제 타입
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forInvalidType(
            @NonNull String key,
            @Nullable String path,
            @NonNull String expected,
            @NonNull String actual
    ) {
        return new YmlMappingException(String.format("Invalid type for '%s' in '%s'. Expected %s, but found %s.",
                key, path, expected, actual));
    }

    /**
     * Enum으로 변환할 수 없는 값일 때 예외를 생성합니다.
     *
     * @param key   키
     * @param path  탐색한 경로
     * @param value 변환에 실패한 값
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forInvalidEnumValue(
            @NonNull String key,
            @Nullable String path,
            @NonNull String value
    ) {
        return new YmlMappingException(String.format("Failed to parse enum for key '%s' in '%s'. Invalid value: '%s'",
                key, path, value));
    }
}
