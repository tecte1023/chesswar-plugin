package dev.tecte.chessWar.infrastructure.persistence.exception;

import dev.tecte.chessWar.common.exception.SystemException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * YML 데이터 매핑 과정에서 발생하는 예외를 나타냅니다.
 * <p>
 * I/O 작업은 성공했으나, 파일 내용의 누락, 타입 불일치, 범위 초과 등 데이터 무결성 문제가 있을 때 발생합니다.
 * 이 예외는 서버 설정 오류로 간주하여 로그에 상세 내용을 기록합니다.
 */
public class YmlMappingException extends SystemException {
    private YmlMappingException(@NonNull String internalMessage) {
        this(internalMessage, null);
    }

    private YmlMappingException(@NonNull String internalMessage, @Nullable Throwable cause) {
        super(internalMessage, cause);
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
        return new YmlMappingException("Required key is missing [Key: %s, Section: %s]".formatted(key, path));
    }

    /**
     * 값의 타입이 유효하지 않을 때 예외를 생성합니다.
     *
     * @param key      키 이름
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
        return new YmlMappingException(
                "Invalid value type [Key: %s, Path: %s, Expected: %s, Actual: %s]".formatted(key, path, expected, actual)
        );
    }

    /**
     * Enum으로 변환할 수 없는 값일 때 예외를 생성합니다.
     *
     * @param key   키 이름
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
        return new YmlMappingException("Invalid enum value [Key: %s, Path: %s, Value: %s]".formatted(key, path, value));
    }

    /**
     * 값의 길이가 기대한 값과 다를 때 예외를 생성합니다.
     *
     * @param path           탐색한 경로
     * @param expectedLength 기대한 길이
     * @param value          잘못된 길이의 실제 값
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forLengthMismatch(
            @Nullable String path,
            int expectedLength,
            @NonNull String value
    ) {
        return new YmlMappingException(
                "Value length mismatch [Section: %s, Expected: %d, Actual: %d, Value: %s]"
                        .formatted(path, expectedLength, value.length(), value)
        );
    }

    /**
     * 값이 허용된 범위를 벗어났을 때 예외를 생성합니다.
     *
     * @param path          탐색한 경로
     * @param expectedRange 기대한 범위 설명
     * @param value         범위를 벗어난 실제 값
     * @param cause         원인이 된 예외
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forValueOutOfBounds(
            @Nullable String path,
            @NonNull String expectedRange,
            @NonNull String value,
            @Nullable Throwable cause
    ) {
        return new YmlMappingException(
                "Value out of bounds [Section: %s, Expected: %s, Actual: %s]".formatted(path, expectedRange, value),
                cause
        );
    }

    /**
     * 게임을 생성하려는데 보드가 없을 때 예외를 생성합니다.
     *
     * @return 생성된 예외 객체
     */
    @NonNull
    public static YmlMappingException forMissingBoard() {
        return new YmlMappingException("Required resource is missing [Resource: Board, Context: Game Creation]");
    }
}
