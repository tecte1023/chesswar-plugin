package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.common.persistence.PersistableState;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceException;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * 단일 엔티티 영속성을 관리하는 추상 리포지토리입니다.
 * <p>
 * 인메모리 캐싱을 통해 접근 성능을 향상시키고, 비동기 저장을 통해 메인 스레드 부하를 줄입니다.
 *
 * @param <V> 관리하는 엔티티의 타입
 */
@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public abstract class AbstractSingleYmlRepository<V> implements PersistableState {
    private final ExceptionDispatcher dispatcher;
    private final YmlFileManager fileManager;
    private final ExecutorService persistenceExecutor;

    private V cache;

    @Override
    @HandleException
    public void load() {
        ConfigurationSection section = fileManager.config().getConfigurationSection(getDataPath());

        if (section == null) {
            return;
        }

        cache = deserialize(section);
    }

    /**
     * 캐시된 엔티티를 반환합니다.
     *
     * @return 찾은 엔티티
     */
    @NonNull
    public Optional<V> find() {
        return Optional.ofNullable(cache);
    }

    /**
     * 엔티티를 저장하고 비동기적으로 반영합니다.
     *
     * @param entity 저장할 엔티티
     */
    public void save(@NonNull V entity) {
        cache = entity;
        persistChangeAsync(serialize(entity));
    }

    /**
     * 현재 저장된 엔티티를 삭제합니다.
     */
    public void delete() {
        cache = null;
        persistChangeAsync(null);
    }

    @Override
    @HandleException
    public void flush() {
        fileManager.set(getDataPath(), cache == null ? null : serialize(cache));
        fileManager.save();
    }

    /**
     * 데이터 섹션에서 엔티티를 역직렬화합니다.
     *
     * @param section 데이터 섹션
     * @return 역직렬화된 엔티티
     */
    @NonNull
    protected abstract V deserialize(@NonNull ConfigurationSection section);

    /**
     * 엔티티를 데이터 맵으로 직렬화합니다.
     *
     * @param entity 직렬화할 엔티티
     * @return 직렬화된 데이터 맵
     */
    @NonNull
    protected abstract Map<String, Object> serialize(@NonNull V entity);

    /**
     * 데이터 저장 경로를 반환합니다.
     *
     * @return 데이터 경로
     */
    @NonNull
    protected abstract String getDataPath();

    private void persistChangeAsync(@Nullable Object value) {
        String path = getDataPath();

        // 데이터 무결성(순서 보장)과 메인 스레드 성능 보호를 위해 전용 스레드에서 비동기 실행
        persistenceExecutor.execute(() -> {
            try {
                fileManager.set(path, value);
                fileManager.save();
            } catch (PersistenceException e) {
                dispatcher.dispatch(e, null, "Async Persistence '" + path + "'");
            }
        });
    }
}
