package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.AbstractSingleYmlRepository;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.concurrent.ExecutorService;

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
            @NonNull ExceptionDispatcher dispatcher,
            @NonNull YmlFileManager fileManager,
            @NonNull ExecutorService persistenceExecutor
    ) {
        super(mapper, dispatcher, fileManager, persistenceExecutor);
    }

    @Override
    public boolean isGameInProgress() {
        return find().isPresent();
    }

    @NonNull
    @Override
    protected String getDataPath() {
        return StatePaths.STATE_PATH;
    }
}
