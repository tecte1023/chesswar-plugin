package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;

/**
 * 체스판 데이터의 영속성을 관리하는 리포지토리 인터페이스입니다.
 */
public interface BoardRepository {
    /**
     * 영속성 저장소에서 모든 체스판 데이터를 불러와 메모리에 적재합니다.
     */
    @SuppressWarnings("unused")
    void loadAll();

    /**
     * 주어진 체스판의 상태를 영속성 저장소에 저장합니다.
     *
     * @param board 저장할 체스판 객체
     */
    void save(Board board);
}
