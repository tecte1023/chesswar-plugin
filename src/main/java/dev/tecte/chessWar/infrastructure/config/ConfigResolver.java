package dev.tecte.chessWar.infrastructure.config;

import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntPredicate;

/**
 * 설정 값을 해석하고 검증하는 역할을 담당하는 클래스입니다.
 * 사용자 설정을 먼저 확인하고, 값이 없거나 유효하지 않으면 기본 설정에서 값을 찾습니다.
 * 두 소스 모두에 유효한 값이 없으면 제공된 기본값을 반환합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ConfigResolver {
    private final FileConfiguration defaultConfig;
    private final YmlFileManager userConfigSource;

    /**
     * 정수 타입의 설정 값을 해석합니다.
     *
     * @param path      설정 값의 경로
     * @param fallback  유효한 값을 찾지 못했을 때 사용할 기본값
     * @param validator 값의 유효성을 검증하는 Predicate
     * @return 해석된 값. 사용자 설정 -> 기본 설정 -> fallback 순서로 반환됩니다.
     */
    public int resolveInt(@NonNull String path, int fallback, @NonNull IntPredicate validator) {
        return tryResolveIntFrom(userConfigSource.getConfig(), path, validator)
                .orElseGet(() -> tryResolveIntFrom(defaultConfig, path, validator)
                        .orElse(fallback));
    }

    /**
     * 제네릭 타입의 설정 값을 해석합니다.
     *
     * @param path     설정 값의 경로
     * @param fallback 유효한 값을 찾지 못했을 때 사용할 기본값
     * @param parser   문자열을 실제 타입으로 변환하는 함수
     * @param <T>      해석할 값의 타입
     * @return 해석된 값. 사용자 설정 -> 기본 설정 -> fallback 순서로 반환됩니다.
     */
    @NonNull
    public <T> T resolve(@NonNull String path, @NonNull T fallback, @NonNull Function<String, T> parser) {
        return tryResolveFrom(userConfigSource.getConfig(), path, parser)
                .orElseGet(() -> tryResolveFrom(defaultConfig, path, parser)
                        .orElse(fallback));
    }

    @NonNull
    private OptionalInt tryResolveIntFrom(
            @NonNull ConfigurationSection config,
            @NonNull String path,
            @NonNull IntPredicate validator
    ) {
        if (config.isSet(path)) {
            int value = config.getInt(path);

            if (validator.test(value)) {
                return OptionalInt.of(value);
            }
        }

        return OptionalInt.empty();
    }

    @NonNull
    private <T> Optional<T> tryResolveFrom(
            @NonNull ConfigurationSection config,
            @NonNull String path,
            @NonNull Function<String, T> parser
    ) {
        return Optional.ofNullable(config.getString(path))
                .filter(s -> !s.isEmpty())
                .flatMap(s -> Optional.ofNullable(parser.apply(s)));
    }
}
