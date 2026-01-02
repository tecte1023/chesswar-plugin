package dev.tecte.chessWar.piece.application.port;

import dev.tecte.chessWar.piece.domain.model.PieceLayout;
import lombok.NonNull;

/**
 * 기물의 초기 배치 정보를 담고 있는 {@link PieceLayout}을 로드하는 책임을 정의하는 포트 인터페이스입니다.
 */
public interface PieceLayoutLoader {
    /**
     * 기물 배치 설계도를 로드합니다.
     *
     * @return 기물 배치 설계도
     */
    @NonNull
    PieceLayout load();
}
