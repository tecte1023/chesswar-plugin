package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.AbstractSingleYmlRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import static dev.tecte.chessWar.game.infrastructure.persistence.GamePersistenceConstants.StatePaths;

/**
 * YML 파일을 사용하여 단일 {@link Game} 객체 데이터의 영속성을 관리하는 {@link GameRepository}의 구현체입니다.
 * <p>
 * 이 클래스는 {@link AbstractSingleYmlRepository}를 확장하여 캐싱 및 비동기 파일 저장 기능을 활용합니다.
 */
@Singleton
public class YmlGameRepository extends AbstractSingleYmlRepository<Game> implements GameRepository {
    @Inject
    public YmlGameRepository(
            @NonNull GameMapper mapper,
            @NonNull YmlFileManager fileManager,
            @NonNull BukkitScheduler scheduler,
            @NonNull JavaPlugin plugin
    ) {
        super(mapper, fileManager, scheduler, plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameInProgress() {
        return find().isPresent();
    }

    @Override
    public void delete() {
        // AbstractSingleYmlRepository에 delete() 메서드가 추가되면 해당 메서드를 호출하도록 수정해야 합니다.
        // 현재는 인터페이스에만 추가되었으므로, 임시로 아무것도 하지 않거나 예외를 던질 수 있습니다.
        // 사용자 요청에 따라 delete() 구현은 나중에 진행합니다.
    }

    @NonNull
    @Override
    protected String getDataPath() {
        return StatePaths.STATE_PATH;
    }
}
