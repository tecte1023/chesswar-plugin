package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * YAML 데이터를 파싱합니다.
 */
@Singleton
public class YmlParser {
    /**
     * 지정된 경로의 설정 섹션을 가져옵니다.
     *
     * @param parent 부모 섹션
     * @param key    키
     * @return 설정 섹션
     * @throws YmlMappingException 섹션이 없을 경우
     */
    @NonNull
    public ConfigurationSection requireSection(@NonNull ConfigurationSection parent, @NonNull String key) {
        return Optional.ofNullable(parent.getConfigurationSection(key))
                .orElseThrow(() -> YmlMappingException.forMissingKey(key, parent.getCurrentPath()));
    }

    /**
     * 지정된 경로의 설정 섹션을 선택적으로 가져옵니다.
     *
     * @param parent 부모 섹션
     * @param key    키
     * @return 찾은 설정 섹션
     */
    @NonNull
    public Optional<ConfigurationSection> findSection(@NonNull ConfigurationSection parent, @NonNull String key) {
        return Optional.ofNullable(parent.getConfigurationSection(key));
    }

    /**
     * 지정된 경로의 필수 값을 가져옵니다.
     *
     * @param section 설정 섹션
     * @param key     키
     * @param type    값의 타입
     * @param <T>     값의 타입
     * @return 찾은 값
     * @throws YmlMappingException 값이 없거나 타입이 일치하지 않을 경우
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
     * 지정된 경로의 값을 선택적으로 가져옵니다.
     *
     * @param section 설정 섹션
     * @param key     키
     * @param type    값의 타입
     * @param <T>     값의 타입
     * @return 찾은 값
     * @throws YmlMappingException 값이 존재하지만 타입이 일치하지 않을 경우
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
     * 지정된 경로의 필수 Enum 상수를 가져옵니다.
     *
     * @param section    설정 섹션
     * @param key        키
     * @param enumParser 문자열 파서 함수
     * @param <T>        Enum 타입
     * @return 찾은 Enum 상수
     * @throws YmlMappingException 값이 없거나 유효한 Enum 상수가 아닐 경우
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
     * 지정된 경로의 Enum 상수를 선택적으로 가져옵니다.
     *
     * @param section    설정 섹션
     * @param key        키
     * @param enumParser 문자열 파서 함수
     * @param <T>        Enum 타입
     * @return 찾은 Enum 상수
     * @throws YmlMappingException 값이 존재하지만 유효한 Enum 상수가 아닐 경우
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
     * 지정된 경로의 필수 식별자를 가져옵니다.
     *
     * @param section 설정 섹션
     * @param key     키
     * @return 식별자
     */
    @NonNull
    public UUID requireUUID(@NonNull ConfigurationSection section, @NonNull String key) {
        String value = requireValue(section, key, String.class);

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw YmlMappingException.forInvalidType(key, section.getCurrentPath(), UUID.class.getSimpleName(), value);
        }
    }

    /**
     * 지정된 경로의 식별자를 선택적으로 가져옵니다.
     *
     * @param section 설정 섹션
     * @param key     키
     * @return 식별자
     */
    @NonNull
    public Optional<UUID> findUUID(@NonNull ConfigurationSection section, @NonNull String key) {
        return findValue(section, key, String.class).map(v -> {
            try {
                return UUID.fromString(v);
            } catch (IllegalArgumentException e) {
                throw YmlMappingException.forInvalidType(key, section.getCurrentPath(), UUID.class.getSimpleName(), v);
            }
        });
    }

    /**
     * 섹션의 키 문자열을 식별자로 변환합니다.
     *
     * @param section 설정 섹션
     * @param key     키
     * @return 식별자
     */
    @NonNull
    public UUID parseKeyAsUUID(@NonNull ConfigurationSection section, @NonNull String key) {
        try {
            return UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            throw YmlMappingException.forInvalidType(key, section.getCurrentPath(), UUID.class.getSimpleName(), key);
        }
    }
}
