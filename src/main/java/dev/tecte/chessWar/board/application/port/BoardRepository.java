package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;

public interface BoardRepository {
    @SuppressWarnings("unused")
    void loadAll();

    void save(Board board);
}
