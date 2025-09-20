package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

/**
 * YAML 설정 파일에서 데이터를 안전하게 파싱하는 유틸리티 클래스입니다.
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
                .orElseThrow(() -> new YmlMappingException("Missing required section: '" + key + "' in '" + parent.getCurrentPath() + "'."));
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
    public <T> T requireValue(@NonNull ConfigurationSection section, @NonNull String key, @NonNull Class<T> type) {
        if (!section.contains(key)) {
            throw new YmlMappingException("Missing required value: '" + key + "' in '" + section.getCurrentPath() + "'.");
        }

        T value = section.getObject(key, type);

        if (value == null) {
            Object rawValue = section.get(key);
            String actualType = rawValue != null ? rawValue.getClass().getSimpleName() : "null";

            throw new YmlMappingException("Invalid type for '" + key + "' in '" + section.getCurrentPath()
                    + "'. Expected " + type.getSimpleName() + ", but found " + actualType + ".");
        }

        return value;
    }

    /**
     * 지정된 경로에서 필수 Enum 상수를 가져옵니다.
     * 값이 존재하지 않거나 유효한 Enum 상수가 아니면 {@link YmlMappingException}을 발생시킵니다.
     * 대소문자를 구분하지 않고 비교합니다.
     *
     * @param section  값을 가져올 {@link ConfigurationSection}
     * @param key      찾고자 하는 값의 키
     * @param enumType 기대하는 Enum 타입
     * @param <T>      Enum 타입
     * @return 찾은 Enum 상수
     * @throws YmlMappingException 키가 없거나, 값이 유효한 Enum 상수가 아닐 경우
     */
    @NonNull
    public <T extends Enum<T>> T requireEnum(@NonNull ConfigurationSection section, @NonNull String key, @NonNull Class<T> enumType) {
        String enumName = requireValue(section, key, String.class);

        try {
            return Enum.valueOf(enumType, enumName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new YmlMappingException("Invalid '" + key + "' value in '" + section.getCurrentPath() + "': " + enumName);
        }
    }
}