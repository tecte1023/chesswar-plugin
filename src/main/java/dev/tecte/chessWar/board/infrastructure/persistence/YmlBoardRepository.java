package dev.tecte.chessWar.board.infrastructure.persistence;

import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import dev.tecte.chessWar.infrastructure.persistence.AbstractYmlRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * YML 파일을 사용하여 체스판 객체 데이터의 영속성을 관리하는 {@link BoardRepository}의 구현체입니다.
 */
@Singleton
public class YmlBoardRepository extends AbstractYmlRepository<UUID, Board> implements BoardRepository {
    @Inject
    public YmlBoardRepository(
            @NonNull JavaPlugin plugin,
            @NonNull YmlFileManager fileManager,
            @NonNull BoardMapper mapper
    ) {
        super(plugin, fileManager, mapper);
    }

    @NonNull
    @Override
    protected String getDataPath() {
        return BoardConstants.State.STATE_PATH;
    }

    @NonNull
    @Override
    protected UUID getKey(@NonNull Board entity) {
        return entity.id();
    }

    @NonNull
    @Override
    protected UUID convertKey(@NonNull String keyString) {
        return UUID.fromString(keyString);
    }
}
