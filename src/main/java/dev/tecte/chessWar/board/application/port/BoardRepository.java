package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;
import lombok.NonNull;

import java.util.Optional;

/**
 * 체스판({@link Board}) 도메인 객체의 영속성을 관리하는 포트 인터페이스입니다.
 * 이 인터페이스는 애플리케이션 계층과 인프라스트럭처 계층 사이의 의존성을 분리하는 역할을 합니다.
 */
public interface BoardRepository {
    /**
     * 주어진 체스판의 상태를 영속성 저장소에 저장합니다.
     *
     * @param board 저장할 체스판 객체
     */
    void save(@NonNull Board board);

    /**
     * 영속성 저장소에서 체스판 데이터를 조회합니다.
     *
     * @return 체스판 데이터가 존재하면 {@link Optional}에 담아 반환하고, 없으면 빈 {@link Optional}을 반환
     */
    @NonNull
    Optional<Board> get();
}
