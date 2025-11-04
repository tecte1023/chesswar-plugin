package dev.tecte.chessWar.infrastructure.persistence;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.common.persistence.PersistableState;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceWriteException;
import dev.tecte.chessWar.port.persistence.SingleYmlMapper;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Optional;

/**
 * YML 파일을 이용해 단일 엔티티의 영속성을 구현하는 추상 리포지토리 클래스입니다.
 * 제네릭 타입 V(Value)를 사용하여 특정 도메인 객체의 CRUD 기능을 재사용할 수 있도록 지원합니다.
 * 인메모리 캐싱을 통해 데이터 접근 성능을 향상시키고, 비동기 저장을 통해 메인 스레드 부하를 줄입니다.
 *
 * @param <V> 리포지토리에서 관리하는 엔티티의 타입
 */
@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public abstract class AbstractSingleYmlRepository<V> implements PersistableState {
    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final YmlFileManager fileManager;
    private final SingleYmlMapper<V> mapper;

    private V cache;

    @NonNull
    protected abstract String getDataPath();

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        ConfigurationSection section = fileManager.getConfig().getConfigurationSection(getDataPath());

        if (section == null) {
            return;
        }

        cache = mapper.fromSection(section);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @HandleException
    public void persistCache() {
        fileManager.set(getDataPath(), cache == null ? null : mapper.toMap(cache));
        fileManager.save();
    }

    /**
     * 단일 엔티티를 캐시에 저장하고, 파일에 비동기적으로 영속화합니다.
     *
     * @param entity 저장할 엔티티
     */
    public void save(@NonNull V entity) {
        cache = entity;
        persistChangeAsync(mapper.toMap(entity));
    }

    /**
     * 캐시된 엔티티를 Optional 형태로 안전하게 반환합니다.
     *
     * @return 캐시된 엔티티가 존재하면 {@link Optional}에 담아 반환하고, 없으면 빈 {@link Optional}을 반환
     */
    @NonNull
    public Optional<V> get() {
        return Optional.ofNullable(cache);
    }

    private void persistChangeAsync(@NonNull Object value) {
        String path = getDataPath();

        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                fileManager.set(path, value);
                fileManager.save();
            } catch (PersistenceWriteException e) {
                log.error("Failed to persist asynchronous change for single entity at path '{}'", path, e);
            }
        });
    }
}
