package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.common.persistence.PersistableState;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceWriteException;
import dev.tecte.chessWar.port.persistence.YmlMapper;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * YML 파일을 이용한 영속성을 구현하는 추상 리포지토리 클래스입니다.
 * 제네릭 타입 K(Key)와 V(Value)를 사용하여 다양한 도메인 객체에 대한 CRUD 기능을 재사용할 수 있도록 지원합니다.
 * 인메모리 캐싱을 통해 데이터 접근 성능을 향상시키고, 비동기 저장을 통해 메인 스레드 부하를 줄입니다.
 *
 * @param <K> 리포지토리에서 관리하는 엔티티의 키 타입
 * @param <V> 리포지토리에서 관리하는 엔티티의 타입
 */
@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public abstract class AbstractYmlRepository<K, V> implements PersistableState {
    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final YmlFileManager fileManager;
    private final YmlMapper<K, V> mapper;

    protected final Map<K, V> cache = new ConcurrentHashMap<>();

    protected abstract String getDataPath();

    @NonNull
    protected abstract K getKey(@NonNull V entity);

    @NonNull
    protected abstract K convertKey(@NonNull String keyString);

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        ConfigurationSection section = fileManager.getConfig().getConfigurationSection(getDataPath());

        if (section == null) {
            return;
        }

        section.getKeys(false).forEach(keyString -> reconstituteEntity(section, keyString));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @HandleException
    public void persistCache() {
        final String dataPath = getDataPath();

        // 캐시에 없는, 즉 삭제된 엔티티가 파일에 남아있는 경우를 처리하기 위해 null로 설정
        fileManager.set(dataPath, null);

        for (Map.Entry<K, V> entry : cache.entrySet()) {
            final String path = dataPath + "." + entry.getKey().toString();

            fileManager.set(path, mapper.toMap(entry.getValue()));
        }

        fileManager.save();
    }

    /**
     * 단일 엔티티를 캐시에 저장하고, 파일에 비동기적으로 영속화합니다.
     * 즉각적인 파일 쓰기를 보장하지 않으며, 서버 성능을 위해 백그라운드에서 처리됩니다.
     *
     * @param entity 저장할 엔티티
     */
    public void save(@NonNull V entity) {
        K key = getKey(entity);

        cache.put(key, entity);
        persistChangeAsync(key.toString(), mapper.toMap(entity));
    }

    @HandleException
    private void reconstituteEntity(@NonNull ConfigurationSection dataSection, @NonNull String keyString) {
        ConfigurationSection entitySection = dataSection.getConfigurationSection(keyString);

        if (entitySection == null) {
            return;
        }

        K key = convertKey(keyString);
        V entity = mapper.fromSection(key, entitySection);

        cache.put(key, entity);
    }

    private void persistChangeAsync(@NonNull String key, @NonNull Object value) {
        final String path = getDataPath() + "." + key;

        // 파일 I/O 작업은 메인 스레드를 블로킹하여 서버 전체에 렉을 유발할 수 있음
        // 비동기 태스크로 실행하여 서버 성능에 미치는 영향을 최소화
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                fileManager.set(path, value);
                fileManager.save();
            } catch (PersistenceWriteException e) {
                log.error("Failed to persist asynchronous change for key '{}'", key, e);
            }
        });
    }
}
