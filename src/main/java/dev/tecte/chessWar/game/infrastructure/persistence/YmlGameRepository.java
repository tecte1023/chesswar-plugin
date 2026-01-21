package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.AbstractSingleYmlRepository;
import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static dev.tecte.chessWar.game.infrastructure.persistence.GamePersistenceConstants.StatePaths;

/**
 * YML 파일을 사용하여 단일 {@link Game} 객체 데이터의 영속성을 관리하는 {@link GameRepository}의 구현체입니다.
 * <p>
 * 이 클래스는 {@link AbstractSingleYmlRepository}를 확장하여 캐싱 및 비동기 파일 저장 기능을 활용합니다.
 */
@Singleton
public class YmlGameRepository extends AbstractSingleYmlRepository<Game> implements GameRepository {
    private final BoardRepository boardRepository;
    private final GameMapper mapper;

    @Inject
    public YmlGameRepository(
            @NonNull BoardRepository boardRepository,
            @NonNull GameMapper mapper,
            @NonNull ExceptionDispatcher dispatcher,
            @NonNull YmlFileManager fileManager,
            @NonNull ExecutorService persistenceExecutor
    ) {
        super(dispatcher, fileManager, persistenceExecutor);
        this.boardRepository = boardRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean isGameInProgress() {
        return find().isPresent();
    }

    @NonNull
    @Override
    protected Game deserialize(@NonNull ConfigurationSection section) {
        return boardRepository.find()
                .map(board -> mapper.fromSection(section, board))
                .orElseThrow(YmlMappingException::forMissingBoard);
    }

    @NonNull
    @Override
    protected Map<String, Object> serialize(@NonNull Game entity) {
        return mapper.toMap(entity);
    }

    @NonNull
    @Override
    protected String getDataPath() {
        return StatePaths.STATE_PATH;
    }
}
