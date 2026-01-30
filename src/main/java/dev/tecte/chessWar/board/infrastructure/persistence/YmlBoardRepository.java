package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.infrastructure.persistence.BoardPersistenceConstants.StatePaths;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.AbstractSingleYmlRepository;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * YML 파일을 사용하여 단일 체스판의 영속성을 관리하는 BoardRepository 구현체입니다.
 */
@Singleton
public class YmlBoardRepository extends AbstractSingleYmlRepository<Board> implements BoardRepository {
    private final BoardMapper mapper;

    @Inject
    public YmlBoardRepository(
            @NonNull BoardMapper mapper,
            @NonNull ExceptionDispatcher dispatcher,
            @NonNull YmlFileManager fileManager,
            @NonNull ExecutorService persistenceExecutor
    ) {
        super(dispatcher, fileManager, persistenceExecutor);
        this.mapper = mapper;
    }

    @NonNull
    @Override
    protected Board deserialize(@NonNull ConfigurationSection section) {
        return mapper.fromSection(section);
    }

    @NonNull
    @Override
    protected Map<String, Object> serialize(@NonNull Board entity) {
        return mapper.toMap(entity);
    }

    @NonNull
    @Override
    protected String getDataPath() {
        return StatePaths.STATE_PATH;
    }
}
