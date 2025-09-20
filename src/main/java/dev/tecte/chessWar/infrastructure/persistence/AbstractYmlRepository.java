package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
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

    /**
     * 데이터가 저장될 YML 파일 내의 최상위 경로를 반환합니다.
     * 예: "board.state"
     *
     * @return 데이터 루트 경로
     */
    @NonNull
    protected abstract String getDataPath();

    /**
     * 주어진 엔티티에서 키를 추출합니다.
     *
     * @param entity 키를 추출할 엔티티
     * @return 엔티티의 키
     */
    @NonNull
    protected abstract K getKey(@NonNull V entity);

    /**
     * YML 파일에서 읽어온 문자열 키를 실제 키 타입으로 변환합니다.
     *
     * @param keyString 변환할 문자열 키
     * @return 변환된 실제 키 객체
     */
    @NonNull
    protected abstract K convertKey(@NonNull String keyString);

    @Override
    public void loadAll() {
        ConfigurationSection section = fileManager.getConfig().getConfigurationSection(getDataPath());

        if (section == null) {
            return;
        }

        for (String keyString : section.getKeys(false)) {
            try {
                ConfigurationSection entitySection = section.getConfigurationSection(keyString);

                if (entitySection == null) {
                    continue;
                }

                K key = convertKey(keyString);

                cache.put(key, mapper.fromSection(key, entitySection));
            } catch (YmlMappingException e) {
                log.warn("Failed to load entity. {}", e.getMessage());
            } catch (Exception e) {
                log.error("An unexpected error occurred while loading entity '{}'.", keyString, e);
            }
        }
    }

    @Override
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
     * 엔티티를 캐시에 저장하고, 파일에 비동기적으로 영속화합니다.
     *
     * @param entity 저장할 엔티티
     */
    public void save(@NonNull V entity) {
        K key = getKey(entity);

        cache.put(key, entity);
        persistChangeAsync(key.toString(), mapper.toMap(entity));
    }

    private void persistChangeAsync(@NonNull String key, Object value) {
        final String path = getDataPath() + "." + key;

        // 파일 I/O 작업은 메인 스레드를 블로킹하여 서버 전체에 렉을 유발할 수 있음
        // 비동기 태스크로 실행하여 서버 성능에 미치는 영향을 최소화
        scheduler.runTaskAsynchronously(plugin, () -> {
            fileManager.set(path, value);
            fileManager.save();
        });
    }
}
