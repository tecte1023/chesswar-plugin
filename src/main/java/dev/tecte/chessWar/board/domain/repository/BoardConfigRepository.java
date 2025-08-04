package dev.tecte.chessWar.board.domain.repository;

import dev.tecte.chessWar.board.domain.model.BoardConfig;
import org.jetbrains.annotations.Contract;

public interface BoardConfigRepository {
    @Contract("-> new")
    BoardConfig getBoardConfig();
}
