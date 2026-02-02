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
 * 단일 도메인 객체의 영속성을 관리합니다.
 * <p>
 * 캐싱을 통해 접근 성능을 높이고, 비동기 저장으로 메인 스레드 부하를 방지합니다.
 *
 * @param <V> 관리할 도메인 객체 타입
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
     * 캐시된 도메인 객체를 찾습니다.
     *
     * @return 찾은 객체
     */
    @NonNull
    public Optional<V> find() {
        return Optional.ofNullable(cache);
    }

    /**
     * 도메인 객체를 저장하고 비동기적으로 반영합니다.
     *
     * @param entity 저장할 객체
     */
    public void save(@NonNull V entity) {
        cache = entity;
        persistChangeAsync(serialize(entity));
    }

    /**
     * 저장된 도메인 객체를 삭제합니다.
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
     * 도메인 객체를 역직렬화합니다.
     *
     * @param section 데이터 섹션
     * @return 역직렬화된 객체
     */
    @NonNull
    protected abstract V deserialize(@NonNull ConfigurationSection section);

    /**
     * 도메인 객체를 직렬화합니다.
     *
     * @param entity 직렬화할 객체
     * @return 직렬화된 맵
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
