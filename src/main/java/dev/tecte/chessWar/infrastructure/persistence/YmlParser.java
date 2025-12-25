package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * YAML 파일에서 데이터를 안전하게 파싱하는 유틸리티 클래스입니다.
 * Bukkit의 {@link ConfigurationSection}에서 특정 키가 존재하지 않거나 타입이 일치하지 않을 경우,
 * {@link YmlMappingException}을 발생시킵니다.
 */
@Singleton
public class YmlParser {
    /**
     * 지정된 경로에서 {@link ConfigurationSection}을 가져옵니다.
     * 섹션이 존재하지 않으면 {@link YmlMappingException}을 발생시킵니다.
     *
     * @param parent 부모 {@link ConfigurationSection}
     * @param key    찾고자 하는 하위 섹션의 키
     * @return 찾은 {@link ConfigurationSection}
     * @throws YmlMappingException 키에 해당하는 섹션이 없을 경우
     */
    @NonNull
    public ConfigurationSection requireSection(@NonNull ConfigurationSection parent, @NonNull String key) {
        return Optional.ofNullable(parent.getConfigurationSection(key))
                .orElseThrow(() -> YmlMappingException.forMissingKey(key, parent.getCurrentPath()));
    }

    /**
     * 지정된 경로에서 필수 값을 특정 타입으로 가져옵니다.
     * 키가 존재하지 않거나, 값이 null이거나, 타입이 일치하지 않으면 {@link YmlMappingException}을 발생시킵니다.
     *
     * @param section 값을 가져올 {@link ConfigurationSection}
     * @param key     찾고자 하는 값의 키
     * @param type    기대하는 값의 타입
     * @param <T>     값의 타입
     * @return 찾은 값
     * @throws YmlMappingException 키가 없거나, 값이 null이거나, 타입이 일치하지 않을 경우
     */
    @NonNull
    public <T> T requireValue(
            @NonNull ConfigurationSection section,
            @NonNull String key,
            @NonNull Class<T> type
    ) {
        if (!section.contains(key)) {
            throw YmlMappingException.forMissingKey(key, section.getCurrentPath());
        }

        T value = section.getObject(key, type);

        if (value == null) {
            Object rawValue = section.get(key);
            String actualType = rawValue != null ? rawValue.getClass().getSimpleName() : "null";

            throw YmlMappingException.forInvalidType(key, section.getCurrentPath(), type.getSimpleName(), actualType);
        }

        return value;
    }

    /**
     * 지정된 경로에서 값을 선택적으로 가져옵니다.
     * 키가 존재하지 않거나 값이 null인 경우 빈 Optional을 반환합니다.
     *
     * @param section 값을 가져올 {@link ConfigurationSection}
     * @param key     찾고자 하는 값의 키
     * @param type    기대하는 값의 타입 클래스
     * @param <T>     값의 타입
     * @return 값이 존재하고 타입이 일치하면 {@link Optional}에 담아 반환, 그렇지 않으면 빈 {@link Optional}
     * @throws YmlMappingException 값이 존재하지만 지정된 타입과 일치하지 않는 경우
     */
    @NonNull
    public <T> Optional<T> findValue(
            @NonNull ConfigurationSection section,
            @NonNull String key,
            @NonNull Class<T> type
    ) {
        if (!section.contains(key)) {
            return Optional.empty();
        }

        T value = section.getObject(key, type);

        if (value == null) {
            Object rawValue = section.get(key);

            if (rawValue == null) {
                return Optional.empty();
            }

            String actualType = rawValue.getClass().getSimpleName();

            throw YmlMappingException.forInvalidType(key, section.getCurrentPath(), type.getSimpleName(), actualType);
        }

        return Optional.of(value);
    }

    /**
     * 지정된 경로에서 필수 Enum 상수를 가져옵니다.
     * 값이 존재하지 않거나 유효한 Enum 상수가 아니면 {@link YmlMappingException}을 발생시킵니다.
     *
     * @param section    값을 가져올 {@link ConfigurationSection}
     * @param key        찾고자 하는 값의 키
     * @param enumParser 문자열을 Optional<T>로 변환하는 팩토리 메소드
     * @param <T>        값의 타입
     * @return 찾은 Enum 상수
     * @throws YmlMappingException 키가 없거나, 값이 유효한 Enum 상수가 아닐 경우
     */
    @NonNull
    public <T> T requireEnum(
            @NonNull ConfigurationSection section,
            @NonNull String key,
            @NonNull Function<String, Optional<T>> enumParser
    ) {
        String value = requireValue(section, key, String.class);

        return enumParser.apply(value)
                .orElseThrow(() -> YmlMappingException.forInvalidEnumValue(key, section.getCurrentPath(), value));
    }

    /**
     * 지정된 경로에서 Enum 상수를 선택적으로 가져옵니다.
     *
     * @param section    값을 가져올 {@link ConfigurationSection}
     * @param key        찾고자 하는 값의 키
     * @param enumParser 문자열을 Enum 타입으로 변환하는 파서 함수
     * @param <T>        Enum 타입
     * @return 값이 존재하고 유효한 Enum 상수면 {@link Optional}에 담아 반환, 값이 없으면 빈 {@link Optional}
     * @throws YmlMappingException 값이 존재하지만 유효한 Enum 상수가 아닌 경우
     */
    @NonNull
    public <T> Optional<T> findEnum(
            @NonNull ConfigurationSection section,
            @NonNull String key,
            @NonNull Function<String, Optional<T>> enumParser
    ) {
        return findValue(section, key, String.class).map(v -> enumParser.apply(v)
                .orElseThrow(() -> YmlMappingException.forInvalidEnumValue(key, section.getCurrentPath(), v)));
    }

    /**
     * 지정된 경로에서 필수 UUID 값을 가져옵니다.
     * 키가 없거나 형식이 올바르지 않으면 예외를 발생시킵니다.
     *
     * @param section 값을 가져올 {@link ConfigurationSection}
     * @param key     찾고자 하는 값의 키
     * @return 파싱된 UUID
     */
    @NonNull
    public UUID requireUUID(@NonNull ConfigurationSection section, @NonNull String key) {
        String value = requireValue(section, key, String.class);

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw YmlMappingException.forInvalidType(key, section.getCurrentPath(), "UUID", value);
        }
    }

    /**
     * 지정된 경로에서 UUID 값을 선택적으로 가져옵니다.
     * 키가 없거나 값이 null인 경우 빈 Optional을 반환합니다.
     * 형식이 올바르지 않은 경우 예외를 발생시킵니다.
     *
     * @param section 값을 가져올 {@link ConfigurationSection}
     * @param key     찾고자 하는 값의 키
     * @return 파싱된 UUID를 담은 Optional, 없으면 빈 Optional
     * @throws YmlMappingException 값이 존재하지만 UUID 형식이 아닌 경우
     */
    @NonNull
    public Optional<UUID> findUUID(@NonNull ConfigurationSection section, @NonNull String key) {
        return findValue(section, key, String.class).map(v -> {
            try {
                return UUID.fromString(v);
            } catch (IllegalArgumentException e) {
                throw YmlMappingException.forInvalidType(key, section.getCurrentPath(), "UUID", v);
            }
        });
    }
}
