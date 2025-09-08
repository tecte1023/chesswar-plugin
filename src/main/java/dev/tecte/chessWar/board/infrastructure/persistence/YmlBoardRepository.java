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
import java.util.logging.Logger;

@Singleton
public class YmlBoardRepository extends AbstractYmlRepository<UUID, Board> implements BoardRepository {
    @Inject
    public YmlBoardRepository(
            @NonNull JavaPlugin plugin,
            @NonNull YmlFileManager fileManager,
            @NonNull BoardMapper mapper,
            @NonNull Logger logger
    ) {
        super(plugin, fileManager, mapper, logger);
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
