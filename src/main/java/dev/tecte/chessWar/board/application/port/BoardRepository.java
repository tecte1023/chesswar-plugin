package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;
import lombok.NonNull;

/**
 * 체스판({@link Board}) 도메인 객체의 영속성을 관리하는 포트 인터페이스입니다.
 * 이 인터페이스는 애플리케이션 계층과 인프라스트럭처 계층 사이의 의존성을 분리하는 역할을 합니다.
 */
public interface BoardRepository {
    /**
     * 영속성 저장소에서 체스판의 데이터를 불러와 메모리에 적재합니다.
     */
    @SuppressWarnings("unused")
    void load();

    /**
     * 주어진 체스판의 상태를 영속성 저장소에 저장합니다.
     *
     * @param board 저장할 체스판 객체
     */
    void save(@NonNull Board board);

    /**
     * 영속성 저장소에 현재 활성화된 체스판이 존재하는지 확인합니다.
     *
     * @return 체스판이 존재하면 true, 그렇지 않으면 false
     */
    boolean isExists();
}
