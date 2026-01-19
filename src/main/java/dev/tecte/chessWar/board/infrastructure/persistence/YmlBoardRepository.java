package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.AbstractSingleYmlRepository;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.concurrent.ExecutorService;

import static dev.tecte.chessWar.board.infrastructure.persistence.BoardPersistenceConstants.StatePaths;

/**
 * YML 파일을 사용하여 단일 체스판 객체 데이터의 영속성을 관리하는 {@link BoardRepository}의 구현체입니다.
 */
@Singleton
public class YmlBoardRepository extends AbstractSingleYmlRepository<Board> implements BoardRepository {
    @Inject
    public YmlBoardRepository(
            @NonNull BoardMapper mapper,
            @NonNull ExceptionDispatcher dispatcher,
            @NonNull YmlFileManager fileManager,
            @NonNull ExecutorService persistenceExecutor
    ) {
        super(mapper, dispatcher, fileManager, persistenceExecutor);
    }

    @NonNull
    @Override
    protected String getDataPath() {
        return StatePaths.STATE_PATH;
    }
}
