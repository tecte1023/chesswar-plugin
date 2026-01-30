package dev.tecte.chessWar.piece.application.port;

import dev.tecte.chessWar.piece.domain.model.PieceLayout;
import lombok.NonNull;

/**
 * 기물 배치 설계도를 로드합니다.
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
