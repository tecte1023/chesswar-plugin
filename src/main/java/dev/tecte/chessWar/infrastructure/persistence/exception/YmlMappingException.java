package dev.tecte.chessWar.infrastructure.persistence.exception;

import dev.tecte.chessWar.common.exception.Loggable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * YML 데이터 매핑 과정에서 발생하는 예외를 나타냅니다.
 * 데이터 파일의 값이 없거나, 타입이 유효하지 않은 경우 발생합니다.
 */
public class YmlMappingException extends RuntimeException implements Loggable {
    private YmlMappingException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    private YmlMappingException(@NonNull String message) {
        this(message, null);
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

    /**
     * 값의 길이가 기대한 값과 다를 때 예외를 생성합니다.
     *
     * @param path           탐색한 경로
     * @param expectedLength 기대한 길이
     * @param key            해당 키의 값
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forLengthMismatch(
            @Nullable String path,
            int expectedLength,
            @NonNull String key
    ) {
        return new YmlMappingException(String.format(
                "Length mismatch for key in section '%s'. Expected %d, but found %d for key '%s'.",
                path, expectedLength, key.length(), key
        ));
    }

    /**
     * 값이 허용된 범위를 벗어났을 때 예외를 생성합니다.
     *
     * @param path          탐색한 경로
     * @param expectedRange 기대한 범위 설명
     * @param key           해당 키
     * @param cause         원인이 된 예외 (없으면 null)
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forValueOutOfBounds(
            @Nullable String path,
            @NonNull String expectedRange,
            @NonNull String key,
            @Nullable Throwable cause
    ) {
        return new YmlMappingException(String.format(
                "Value for key in section '%s' is out of bounds. Expected bounds '%s', but found '%s'.",
                path, expectedRange, key
        ), cause);
    }

    /**
     * 게임을 생성하려는데 보드가 없을 때 예외를 생성합니다.
     *
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forMissingBoard() {
        return new YmlMappingException("The board does not exist. It must be created before creating a game.");
    }
}
