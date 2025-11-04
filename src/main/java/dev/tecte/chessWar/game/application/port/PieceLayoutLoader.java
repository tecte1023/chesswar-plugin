package dev.tecte.chessWar.game.application.port;

import dev.tecte.chessWar.game.domain.model.PieceLayout;
import lombok.NonNull;

/**
 * 체스 말의 초기 배치 정보를 담고 있는 {@link PieceLayout}을 로드하는 책임을 정의하는 포트 인터페이스입니다.
 * 이 인터페이스는 애플리케이션 계층과 인프라스트럭처 계층 사이의 의존성을 분리하는 역할을 합니다.
 */
public interface PieceLayoutLoader {
    /**
     * 기물 배치 설계도를 로드합니다.
     *
     * @return 기물 배치 설계도
     * @throws IllegalStateException 로드에 필요한 조건(몹 부재)이 충족되지 않았을 경우
     */
    @NonNull
    PieceLayout load();
}
