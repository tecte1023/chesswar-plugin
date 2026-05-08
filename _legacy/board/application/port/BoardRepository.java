package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;
import lombok.NonNull;

import java.util.Optional;

/**
 * 체스판의 영속성을 관리합니다.
 */
public interface BoardRepository {
    /**
     * 현재 생성된 체스판을 찾습니다.
     *
     * @return 찾은 체스판
     */
    @NonNull
    Optional<Board> find();

    /**
     * 체스판을 저장하거나 업데이트합니다.
     *
     * @param board 저장할 체스판
     */
    void save(@NonNull Board board);
}
