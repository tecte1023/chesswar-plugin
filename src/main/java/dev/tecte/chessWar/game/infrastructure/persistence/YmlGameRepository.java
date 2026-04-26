package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.game.application.GameTimerService;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.infrastructure.persistence.GamePersistenceConstants.StatePaths;
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

/**
 * 게임 저장소의 YAML 구현체입니다.
 */
@Singleton
public class YmlGameRepository extends AbstractSingleYmlRepository<Game> implements GameRepository {
    private final BoardRepository boardRepository;
    private final GameTimerService timerService;
    private final GameMapper mapper;

    @Inject
    public YmlGameRepository(
            @NonNull BoardRepository boardRepository,
            @NonNull GameTimerService timerService,
            @NonNull GameMapper mapper,
            @NonNull ExceptionDispatcher dispatcher,
            @NonNull YmlFileManager fileManager,
            @NonNull ExecutorService persistenceExecutor
    ) {
        super(dispatcher, fileManager, persistenceExecutor);
        this.boardRepository = boardRepository;
        this.timerService = timerService;
        this.mapper = mapper;
    }

    @Override
    public boolean isGameInProgress() {
        return find().isPresent();
    }

    @Override
    public void save(@NonNull Game game) {
        super.save(captureRealTime(game));
    }

    @NonNull
    @Override
    protected Game deserialize(@NonNull ConfigurationSection section) {
        return boardRepository.find()
                .map(board -> mapper.fromSection(section, board))
                .orElseThrow(() -> YmlMappingException.forMissingResource(Board.class));
    }

    @NonNull
    @Override
    protected Map<String, Object> serialize(@NonNull Game game) {
        return mapper.toMap(captureRealTime(game));
    }

    // 실시간 남은 시간을 반영하여 데이터 정합성 확보
    private Game captureRealTime(Game game) {
        return timerService.remainingTime()
                .map(game::remaining)
                .orElse(game);
    }

    @NonNull
    @Override
    protected String getDataPath() {
        return StatePaths.STATE_PATH;
    }
}
